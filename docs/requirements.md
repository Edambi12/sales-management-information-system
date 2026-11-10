# Requirements and implementation status

Requirements are taken from the internship report
([`project-report.pdf`](project-report.pdf), chapter 2 "Spécification des besoins").
Each one was then checked against the Java code (`src/main/java/projet`) and the
database schema, and exercised by the integration test
([`tests/integration/SwingIntegrationTest.java`](../tests/integration/SwingIntegrationTest.java)).

Legend — **✅ Implemented & tested** · **🟡 Partial** · **❌ Not implemented** (documented in the report only)

## 1. Actors

| Report | Application |
|---|---|
| Seller (*vendeur*), customer, treasurer (*trésorier*), storekeeper (*magasinier*) | A single **administrator** account (`administration` table). There is no role-based access control: every authenticated user reaches every module. The *customer* actor of the use-case diagram does not use the application directly. |

## 2. Functional requirements

### A. Authentication — `LoginSoktrapa`, `MenuPrincipal`
| Requirement | Status | Notes |
|---|---|---|
| Administrator login (user + password) | ✅ | Parameterised query; PBKDF2 hash verification added in this repository (legacy plain-text values still accepted with a warning). |
| Main menu giving access to the five modules | ✅ | Clients, Stock, Commandes, Paiements, Réclamations. |
| Logout / session | ❌ | Closing a module returns to the menu; there is no logout or session object. |

### B. Customer management — `GestionClients`
| Requirement | Status | Notes |
|---|---|---|
| Create, view, update, delete customers | ✅ | Confirmation dialog before deletion. |
| Search | ✅ | By name, first name, ID, phone or type (`LIKE`, parameterised). |
| Customer type, payment terms, credit limit | ✅ | Combo boxes (Entreprise/Grossiste/Détaillant; Comptant, 30/60/90 jours, Fin de mois); numeric validation of the credit limit. |
| Credit limit enforcement on orders | ❌ | Stored and displayed only; not checked when ordering. |
| Customer balance (*solde client*) | ❌ | Mentioned in the analysis, not in the schema. |

### C. Order management — `GestionCommandes`
| Requirement | Status | Notes |
|---|---|---|
| Create / update / delete orders; search by ID | ✅ | Order date, expected delivery date, discount %, customer selection. |
| Order status (validate / cancel) | ✅ | Status `En attente`, `Confirmée`, `Annulée`. |
| Order lines: product + quantity (add / modify / delete) | ✅ | Transactions (`setAutoCommit(false)`, commit/rollback). |
| Check stock before accepting a line | ✅ | Blocking when insufficient; alerts at low levels. |
| Reserve / decrease stock | ✅ | Stock is decreased immediately when a line is added and restored when it is removed. |
| Trigger a purchase when out of stock | ❌ | Only a warning message ("passer une commande d'approvisionnement"). Purchasing/suppliers are out of scope. |

### D. Inventory management — `GestionStock`
| Requirement | Status | Notes |
|---|---|---|
| Consult stock (depots, lots, locations, quantities) and product↔depot mapping | ✅ | |
| Inventory (physical count, gap detection, adjustment) | ✅ | |
| Stock exit (*sortie stock PF*) | ✅ | Refused above available quantity. |
| Check availability | ✅ | By depot ID. |
| Create / delete depots and products | ❌ | No UI; data is maintained in the database (the class diagram lists `ajoutProduit()`, `supprimer()` …). |
| Movement history, FIFO/LIFO lots, reservations | ❌ | `mouvement_depot` is a single label per depot, not a journal. |

### E. Sales, invoices and payments — `GestionPaiement`
| Requirement | Status | Notes |
|---|---|---|
| Create an invoice from a confirmed order | ✅ | Detailed invoice preview, discount, 7 % VAT, NET TTC computed automatically. |
| Consult invoices | ✅ | Table with customer name. |
| Record a payment (*encaissement*) | ✅ | Amount, date, status, method (cash, cheque, transfer, card, bill, other). |
| Update / delete / search payments | ✅ | Search by payment ID. |
| Remaining balance | ✅ | Per payment row (invoice amount − payment amount). |
| Update / delete invoices, print invoice, receipt | ❌ | Listed in the class diagram (`modifFacture()`, `imprimerFacture()`, `genererRecu()`), not implemented. |

### F. Returns and complaints — `Reclamation`
| Requirement | Status | Notes |
|---|---|---|
| Create a return request linked to an invoice | ✅ | Reason and product condition from predefined lists. |
| Update / delete / consult / search returns | ✅ | |
| Validate / refuse a return, analyse reasons | 🟡 | No explicit decision field in the schema; the report's sequence diagram describes it. |
| Automatic stock update on return | ❌ | The use-case diagram shows `<<include>> mise à jour stock`; the code does not change stock on returns. |

## 3. Non-functional characteristics (observed)

| Aspect | Observation |
|---|---|
| Platform | Java SE desktop (Swing), runs on Windows/Linux/macOS with JDK 17+. |
| Persistence | MySQL/MariaDB over JDBC; parameterised queries for all user input. |
| Usability | French UI, consistent green theme, confirmation dialogs, validation messages. |
| Security | See [`security-audit.md`](security-audit.md). |
| Tests | Automated Swing/JDBC integration test (38 checks) — no unit-test framework. |
