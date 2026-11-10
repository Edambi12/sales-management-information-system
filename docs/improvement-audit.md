# Technical improvement audit — order/stock consistency, delete safety, architecture

Second audit pass, focused on the weaknesses left open by the first one
([`security-audit.md`](security-audit.md)). Each item is marked **Done** (changed and
covered by a test in this repository) or **Recommended** (not implemented).

## 1. Order and inventory consistency

**Invariant now enforced:** a quantity is removed from the depot for as long as its order
line exists. Order lines and depot quantities always change together, in one transaction.

### Defects found in the original code

| # | Defect | Effect | Status |
|---|---|---|---|
| C1 | Deleting an order deleted its lines without restoring their stock. | Stock permanently lost on every order deletion. | **Done** — test R1 |
| C2 | Deleting or modifying a line used the quantity **displayed in the table**, not the one in the database. | If the line had changed since the screen was loaded, the wrong quantity was restored. | **Done** — tests R5, R6 |
| C3 | `diminuerStock` updated **every** depot of the product that had enough stock; `restaurerStock` added the quantity to **every** depot. | With a product in two depots, one order line moved the quantity twice. | **Done** — test R7 |
| C4 | The stock decrease ignored the number of updated rows. | A decrease that matched no depot was reported as a success. | **Done** — the update must affect exactly one row, otherwise the transaction is rolled back |
| C5 | Stock was read and then updated without a row lock. | Two sessions could both pass the control and oversell. | **Done** — `SELECT … FOR UPDATE` on the depot and on the order line |
| C6 | Stock alert dialogs were shown while the transaction was open. | Database rows stayed locked until the user clicked OK. | **Done** — dialogs are shown after commit/rollback |
| C7 | Stock exit and inventory (`GestionStock`) read then updated outside a transaction. | Same race as C5. | **Done** — transaction + `FOR UPDATE` |
| C8 | Deleting a customer cascade-deleted their order lines without restoring stock. | Stock lost. | **Done** — prevented by `ON DELETE RESTRICT` (section 2), test R9 |

### How it is implemented

The rules moved out of the Swing window into a small service layer (section 3):

* `CommandeService.ajouterLigne / modifierLigne / supprimerLigne / supprimerCommande`
  each open one transaction, lock the rows they read, and roll back on any refusal or error.
* A refused operation (insufficient stock, product without depot, duplicate line,
  invoiced order) leaves **both** the lines and the stock exactly as they were — tests R2, R3, R4, R8.
* Depot rule when a product is stored in several depots: the quantity is taken from the
  depot holding the most stock and returned to the product's first depot. Exactly one depot
  row changes each time, so the product's total is conserved.

### Recommended (not implemented)

| Topic | Why | Suggested approach |
|---|---|---|
| Cancelling an order (status `Annulée`) keeps its stock reserved. | A cancelled order should normally release its stock; today the user must delete the lines or the order. | Release on transition to `Annulée`, re-reserve (with a stock control) when reactivated, and block line edits on cancelled orders. |
| Lines of an **invoiced** order can still be edited. | The invoice amount then no longer matches the order (this was visible in the original test data). | Refuse line changes once an invoice exists, or issue a credit note. |
| The depot that served a line is not stored. | With several depots per product, the quantity cannot be returned to the exact depot it came from. | Add `id_depot` to `ligne_commande` (or a stock-movement table). |
| A quantity larger than the biggest depot is refused even if the product's total would cover it. | Original behaviour, kept. | Split a line across depots. |
| No stock movement history. | No audit trail of who changed stock and why. | `mouvement_stock` table written by the service. |

## 2. Database delete safety

### Risk assessment of the original rules

Every foreign key was `ON DELETE CASCADE`:

| Deleting… | Silently removed | Risk |
|---|---|---|
| a customer | their orders, order lines, invoices, payments, returns | **Critical** — complete commercial and financial history destroyed by one click; reserved stock lost |
| an order | its lines, invoices, payments, returns | **High** — invoices and payments are accounting records |
| an invoice | its payments and returns | **High** — only possible through SQL (no screen), still unsafe |
| a product | every order line that used it | **High** — past orders altered |
| a depot / product | `stocker` association rows | Low — association only |

### Done

Six foreign keys are now `ON DELETE RESTRICT` (`ON UPDATE CASCADE` kept):

| Child → parent | Rule | Consequence in the application |
|---|---|---|
| `commande` → `client` | RESTRICT | A customer with orders cannot be deleted. The existing message *"Impossible: ce client est lié à d'autres données!"* — unreachable before — is now shown. Test R9. A customer without orders can still be deleted (test R10). |
| `ligne_commande` → `commande` | RESTRICT | Lines can only be removed by the application, which restores stock first. |
| `ligne_commande` → `produit` | RESTRICT | A product used in orders cannot be deleted. |
| `facture` → `commande` | RESTRICT | An invoiced order cannot be deleted; the order window says so and changes nothing. Test R8. |
| `paiement` → `facture`, `retour` → `facture` | RESTRICT | An invoice with payments or returns cannot be deleted. |
| `stocker` → `produit` / `depot` | CASCADE (unchanged) | Pure association rows. |

* New installations: [`database/schema.sql`](../database/schema.sql).
* Existing databases: [`database/migrations/001_restrict_deletes.sql`](../database/migrations/001_restrict_deletes.sql)
  (changes constraints only; verified to give the same rules as a fresh install without touching any row).
* Features preserved: deleting orders without invoice, order lines, payments, returns and
  customers without orders works as before.

### Recommended (not implemented)

* **Soft delete for customers** (`actif` flag or `date_archivage` column, filtered in the
  lists) so that a customer with history can be retired without losing records. This needs a
  schema column plus changes to the customer and order screens.
* **Invoice cancellation** through the existing `Annulée` status or a credit note instead of
  physical deletion.
* A `UNIQUE` constraint on `facture.id_commande` if the business rule "one invoice per order"
  is confirmed (the screen already enforces it).

## 3. Architecture

### Assessment

In the original code each window (`JFrame`) contains the widgets, the event handlers, the
business rules and the SQL. Consequences observed during this audit:

* the stock rules could not be tested without opening a window and answering dialogs;
* a business decision (`controlerStock`) opened dialogs itself, inside a transaction (C6);
* the same queries and helper code are repeated in several windows;
* a rule could not be reused: order deletion did not restore stock although the logic
  existed a few methods further down (C1).

### Done — first vertical slice

The order-line / stock rules now follow a Model → DAO → Service → UI layering.
The other modules are unchanged.

| Layer | Class | Role |
|---|---|---|
| Model | `projet.model.ControleStock`, `projet.model.SuppressionCommande` | Immutable results, no Swing or JDBC |
| DAO | `projet.dao.StockDao`, `projet.dao.LigneCommandeDao` | SQL only; work on the caller's connection, never commit |
| Service | `projet.service.CommandeService` | Business rules and transaction boundaries |
| UI | `projet.GestionCommandes` | Validation of the form, call the service, display the result |

`GestionCommandes` lost about 115 lines of SQL and transaction handling; every dialog text
is unchanged.

### Recommended — gradual migration path

Each step is independent, keeps the application working, and can reuse the integration test.

1. **Order header** — `CommandeDao` + model `Commande`; move create/update/search out of `GestionCommandes`.
2. **Invoices and payments** — `FactureService.calculer(idCommande)` returning a model with
   gross amount, discount, VAT and net amount (pure computation, unit-testable), then `FactureDao`, `PaiementDao`.
3. **Customers** — `ClientDao` + model `Client`.
4. **Stock screen** — reuse `StockDao` for inventory and stock exit.
5. **Returns** — `RetourDao`.
6. **Cross-cutting** — a shared `Transaction` helper to remove the repeated commit/rollback
   blocks, shared UI helpers for the duplicated styling code, then JUnit tests for the services.

## 4. Testing

`tests/integration/SwingIntegrationTest.java` — run with `tests/run-integration-tests.sh`.

| Run | Result |
|---|---|
| New code, new schema (MariaDB 10.11, least-privilege account) | **59 / 59 pass** — the 38 earlier checks plus 21 regression checks (R1–R12); repeatable |
| Same tests against the previous order window and the previous cascade schema | 15 fail — R1, R5, R6, R7, R8, R9 fail directly; R2–R4, R12 and the final dataset check fail as a consequence of the stock lost in R1 |
| Migration script on a database created with the previous schema | Constraint rules identical to a fresh install; row counts unchanged |

Not covered: true concurrent sessions (the locking is in place but there is no
multi-threaded test), and MySQL 8 (tests ran on MariaDB only).
