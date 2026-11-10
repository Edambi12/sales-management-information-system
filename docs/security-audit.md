# Security & code audit

Scope: the original Eclipse project (`projet.zip`, 7 Java classes, ~7,100 lines), the
original database export (`soktrapa.sql`) and the internship report, reviewed before
publication of this public repository.

> A second, focused pass (order/stock consistency, delete safety, architecture) is reported in
> [`improvement-audit.md`](improvement-audit.md). Items it resolved are marked *second pass* below.

Status legend — **Fixed** (changed in this repository) · **Mitigated** · **Documented** (known limitation, left as-is to preserve the original behaviour).

## 1. Findings

| # | Severity | Finding | Status |
|---|---|---|---|
| S1 | High | **Hardcoded DB credentials** (`root` / empty password) duplicated in 6 classes. | **Fixed** — removed from the source; `DatabaseConfig` reads `config/database.properties` (git-ignored) or `SOKTRAPA_DB_*` environment variables. A placeholder file `config/database.properties.example` is committed. |
| S2 | High | **Plain-text administrator password** in the SQL export (default-style `admin` credentials) and plain-text comparison in SQL (`WHERE password = ?`). | **Fixed** — the original export is not published; the demo dataset stores a **PBKDF2-HMAC-SHA256** hash (210k iterations, 16-byte random salt). Login reads the stored value and verifies it in Java with a constant-time comparison. Legacy plain-text rows still work (backward compatibility) but log a warning. `projet.PasswordHasher` generates hashes. |
| S3 | High | **Personal customer data**: names, phone numbers, e-mail addresses in the SQL export and in four report screenshots. | **Fixed** — fictional customers on reserved domains (`example.com`); customer columns redacted in the screenshots **and** inside the published PDF (the embedded images were replaced, not just covered; verified that no original image object remains). |
| S4 | Medium | Application connected as MySQL **`root`**. | **Mitigated** — documentation and example config use a dedicated account with `SELECT, INSERT, UPDATE, DELETE` on `soktrapa.*` only; the integration test runs with exactly these rights. |
| S5 | Medium | DB errors during login were reported as *"wrong user or password"* (exception swallowed). | **Fixed** — `SQLException` is propagated and the existing error dialog ("Erreur de connexion à la base de données") is shown. |
| S6 | Info | **SQL injection**: every query that contains user input uses `PreparedStatement`; `Statement` is only used for constant queries. Verified by review and by an injection-style login test (`admin' OR '1'='1`). | No issue found. |
| S7 | Medium | Authentication is UI-level only: every module class has its own `main()` and can be started without logging in; there are no roles. | **Documented** (database privileges are the real boundary). |
| S8 | Medium | **`ON DELETE CASCADE`** on all FKs: deleting a customer deleted their orders, invoices, payments and returns. The UI's "linked data" error message for customer deletion could therefore never appear. | **Fixed** (second pass) — six foreign keys are now `ON DELETE RESTRICT`; see [`improvement-audit.md`](improvement-audit.md). |
| S9 | Low | No account lockout / rate limiting on login. | Documented. |
| S10 | Info | Destructive SQL: the original export contained no `DROP` statements. The published scripts are non-destructive (no `DROP DATABASE` / `DROP TABLE`). | OK. |

## 2. Java code review

| # | Type | Finding | Status |
|---|---|---|---|
| J1 | Bug | `GestionStock` read `depot.lot_depot` (VARCHAR) with `getInt()` in 3 places → `SQLException` as soon as a lot is not purely numeric (e.g. `L-2405`). | **Fixed** — read with `getString()`. |
| J2 | Portability | `.classpath` referenced the JDBC driver by an absolute Windows path (`C:/Soktrapa ETUDE DE CAS/...`). | **Fixed** — project-relative `lib/mysql-connector-j-9.6.0.jar` + optional Maven dependency. |
| J3 | Portability | Compiler level Java 22 (non-LTS). The code uses no feature newer than Java 9. | **Changed** to Java 17 (LTS) in Eclipse settings and Maven, so it builds on any JDK ≥ 17. |
| J4 | Logic | Deleting a whole order deleted its lines but did **not** restore their stock (deleting a single line did). | **Fixed** (second pass) — `CommandeService.supprimerCommande`, test R1. |
| J5 | Logic | `controlerStock` checks the depot with the highest quantity, while `diminuerStock` updates *every* depot linked to the product that has enough stock. Equivalent with the original data (1 depot per product) but over-decremented with several depots. | **Fixed** (second pass) — exactly one depot row is updated, test R7. |
| J6 | Logic | Stock exit / inventory in `GestionStock` were not wrapped in a transaction (read then update), and are not recorded as movements (no history table). | Transaction + row lock **fixed** (second pass); movement history still **documented**. |
| J7 | Design | IDs are typed manually by the user although the columns are `AUTO_INCREMENT`. | Documented. |
| J8 | Resources | A few `PreparedStatement`/`ResultSet` objects are not in their own try-with-resources; they are released when the enclosing connection is closed. | Documented (no leak in practice). |
| J9 | Design | UI, business rules and SQL in the same `JFrame` classes; colour/button helpers duplicated. | **Partly addressed** (second pass) — order-line/stock rules extracted to a DAO/service layer; other modules documented, see `architecture.md`. |
| J10 | Data | Status strings appear both as `Confirmée` and `Confirmee` in the original data; the code accepts both. | Demo data normalised. |
| J11 | UX | Modal dialogs were shown while a DB transaction was open (stock alerts). | **Fixed** (second pass) — dialogs are shown after the transaction ends. |
| J12 | Config | VAT rate (7 %) and stock alert thresholds (1000 kg, 50 %) are hardcoded. | Documented. |

## 3. Password hashing recommendation (implemented)

* Never store or compare passwords in plain text, and never send the password to the
  database inside a `WHERE` clause.
* Use a slow, salted key-derivation function: **Argon2id** or **bcrypt** via a library,
  or **PBKDF2-HMAC-SHA256** (≥ 210,000 iterations, OWASP 2023) which is built into the JDK —
  chosen here to avoid adding dependencies.
* Store `algorithm$iterations$salt$hash` so parameters can be raised later; compare with
  `MessageDigest.isEqual` (constant time).
* Migrate legacy rows: generate a hash with `PasswordHasher` and update the row; then
  remove the plain-text fallback in `LoginSoktrapa.verifyCredentials`.

## 4. Publication checklist

- [x] No real credentials in source, config or SQL (`git grep -i password` reviewed).
- [x] `config/database.properties` and `lib/*.jar` ignored by Git.
- [x] No `.class` files or `bin/` output committed.
- [x] Original `soktrapa.sql` export not published.
- [x] Customer data replaced (SQL) and redacted (screenshots, PDF).
- [x] Report published as `docs/project-report.pdf` (redacted copy).
