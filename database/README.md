# Database — `soktrapa`

MySQL / MariaDB database used by the SOKTRAPA desktop application.
Full design documentation (table dictionary, keys, ER diagram, discrepancies with
the report): [`docs/database-design.md`](../docs/database-design.md).

| File | Content |
|------|---------|
| [`schema.sql`](schema.sql) | Database + 10 tables, primary keys, indexes, 8 foreign keys. Non-destructive (no `DROP`). |
| [`sample_data.sql`](sample_data.sql) | **Fictional** demonstration dataset (4 customers, 8 products, 8 depots, 9 orders, 18 order lines, 5 invoices, 5 payments, 2 returns, 1 demo administrator). |

## 1. Initialise the database

```bash
mysql -u root -p < database/schema.sql
```

```bash
mysql -u root -p soktrapa < database/sample_data.sql
```

With XAMPP on Windows you can also use **phpMyAdmin → Import** and import the two
files in that order.

To reset a local demo database:

```bash
mysql -u root -p -e "DROP DATABASE soktrapa"
```

then run the two import commands again. (This `DROP` is intentionally *not* part of the scripts.)

## 2. Create a least-privilege application account (recommended)

The original project connected as `root` with an empty password. The application only
needs data manipulation rights, so create a dedicated account and put it in
`config/database.properties`:

```sql
CREATE USER 'soktrapa_app'@'localhost' IDENTIFIED BY 'choose-a-strong-password';
GRANT SELECT, INSERT, UPDATE, DELETE ON soktrapa.* TO 'soktrapa_app'@'localhost';
FLUSH PRIVILEGES;
```

The integration test suite (`tests/run-integration-tests.sh`) was run with exactly
these privileges.

## 3. Demo login

| User | Password | Storage |
|------|----------|---------|
| `admin` | `Soktrapa@Demo2026` | PBKDF2-HMAC-SHA256 hash (210,000 iterations, random salt) |

These credentials exist **only** for the local demonstration dataset.
To create or change an administrator password:

```bash
java -cp target/classes projet.PasswordHasher "NewStrongPassword"
```

```sql
UPDATE administration SET password = '<hash printed above>' WHERE nom_utilisateur = 'admin';
```

(In Eclipse: run `PasswordHasher` with the password as program argument.)

## 4. About the sample data

* **Customers are fictional**: names, addresses, phone numbers (`+216 70 000 0xx`) and
  e-mail addresses on reserved domains (`example.com`, `.example`).
  The original export contained real-looking customer records and a plain-text
  administrator password; it is **not** published.
* Products, depots, orders and order lines come from the original test data (no personal data).
* Invoice amounts were recomputed from the order lines with the application's own formula
  `NET TTC = Σ(price × qty) × (1 − discount%) × 1.07` so that every invoice is consistent
  with its order; duplicate invoices of the original test data were removed and payment
  statuses were aligned with the balances. Details are in the header of `sample_data.sql`.

## 5. Verification performed

On MariaDB 10.11 (the original export came from MariaDB 10.4 / XAMPP):

* `schema.sql` + `sample_data.sql` import without errors.
* Column names, types, nullability and keys are **identical** to the original export
  (compared through `information_schema.columns` and `information_schema.key_column_usage`).
* All 8 foreign keys are present; every invoice amount equals the recomputed NET TTC.
