-- =============================================================================
-- SOKTRAPA - Sales Management Information System
-- FICTIONAL demonstration dataset
--
-- Run after database/schema.sql:
--   mysql -u <user> -p soktrapa < database/sample_data.sql
--
-- PRIVACY: every customer below is fictional. Names, addresses, phone numbers
-- and e-mail addresses (example.com / .example, RFC 2606 reserved domains) were
-- created for this public repository and replace the records of the original
-- local database export, which is NOT published.
--
-- Product catalogue, depots, orders, order lines, invoice amounts and returns are
-- taken from the original test data (they contain no personal information), so the
-- business rules (stock control, invoice totals, remaining balance) can be
-- demonstrated on realistic figures. Small consistency clean-ups were applied:
--   * duplicate invoices #2 and #9 (same order #1 as invoice #1) were removed;
--   * order #1 status 'Confirmee' normalised to 'Confirmée';
--   * invoice amounts/quantities recomputed from the current order lines with the
--     application's formula (gross - discount + 7% VAT), because some order lines
--     had been edited after invoicing in the original test data;
--   * payment rows and statuses adjusted so balances are coherent
--     (partially paid invoices stay 'En attente').
--
-- DEMO LOGIN (local demonstration only - never use in production):
--   user     : admin
--   password : Soktrapa@Demo2026
-- The password is stored as a PBKDF2-HMAC-SHA256 hash (projet.PasswordHasher).
-- =============================================================================

USE `soktrapa`;
SET NAMES utf8mb4;
START TRANSACTION;

-- Administrator (hashed demo password)
INSERT INTO `administration` (`id`, `nom_utilisateur`, `password`) VALUES
(1, 'admin', 'pbkdf2_sha256$210000$v7IDJXs8ydITPkORQMMo/g==$yrA9nHIDFVF5dxGm/Xfm+ihfTvxK/tupiz2l9ETNiHg=');

-- Customers (fictional)
INSERT INTO `client` (`id_client`, `nom_client`, `prenom_client`, `adress_client`, `tel_client`, `email_client`, `type_client`, `condition_paiement`, `plafond_credit`) VALUES
(1, 'Ben Salem', 'Karim',   'Zone industrielle (demo), Sfax', '+216 70 000 001', 'karim.bensalem@example.com',  'Entreprise', '90 jours', 2000),
(2, 'Trabelsi',  'Sonia',   'Route de Tunis (demo), Sfax',    '+216 70 000 002', 'sonia.trabelsi@example.com',  'Entreprise', '30 jours', 2100),
(3, 'Gharbi',    'Youssef', 'Medina (demo), Sfax',            '+216 70 000 003', 'youssef.gharbi@example.com',  'Détaillant', 'Comptant', 4000),
(4, 'Demo Olives SARL', 'Service achats', 'Route de Mahdia (demo), Sfax', '+216 70 000 004', 'achats@demo-olives.example', 'Grossiste', 'Comptant', 5000);

-- Products (finished goods catalogue: olives, pickles, mixes)
INSERT INTO `produit` (`id_produit`, `nom_produit`, `prix_produit`, `type_produit`) VALUES
(1, 'pitcha',          2.8, 'olive'),
(2, 'meski',           3,   'olive'),
(3, 'maksous',         2.9, 'olive'),
(4, 'variante',        1.5, 'mélange'),
(5, 'Concombre',       2.9, 'cornichon'),
(6, 'ziton akhal',     1.5, 'olive'),
(7, 'besbes imalh',    2.9, 'besbes'),
(8, 'seneriya imalha', 2.7, 'seneria');

-- Depots / lots (quantities in kg)
INSERT INTO `depot` (`id_depot`, `lot_depot`, `lieu_depot`, `mouvement_depot`, `qte_disponible`) VALUES
(1, '2405', 'p5', 'entré', 100),
(2, '2408', 'p6', 'entré', 2000),
(3, '2406', 'p7', 'entré', 2375),
(4, '2407', 'p8', 'entré', 18425),
(5, '2410', 'p1', 'entré', 2000),
(6, '2411', 'p2', 'entré', 1400),
(7, '2417', 'p3', 'entré', 1360),
(8, '2418', 'p4', 'entré', 5375);

-- Product <-> depot association
INSERT INTO `stocker` (`id_produit`, `id_depot`) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8);

-- Orders
INSERT INTO `commande` (`id_commande`, `date_commande`, `statut`, `date_prevu`, `remise`, `id_client`) VALUES
(1,  '2026-04-11', 'Confirmée',  '2026-04-11', 10, 2),
(3,  '2026-04-22', 'Confirmée',  '2026-04-25', 11, 4),
(8,  '2026-04-01', 'Confirmée',  '2026-04-11', 10, 3),
(14, '2026-04-21', 'En attente', '2026-04-21', 0,  2),
(17, '2026-04-21', 'Confirmée',  '2026-04-29', 10, 4),
(43, '2026-04-15', 'En attente', '2026-04-15', 0,  1),
(44, '2026-04-11', 'Confirmée',  '2026-04-11', 10, 1),
(50, '2026-04-11', 'En attente', '2026-04-11', 0,  1),
(70, '2026-04-15', 'Confirmée',  '2026-04-15', 10, 2);

-- Order lines (quantities in kg)
INSERT INTO `ligne_commande` (`id_commande`, `id_produit`, `qte_commande`) VALUES
(1, 3, 1050), (1, 7, 100), (1, 8, 1047),
(3, 1, 1300), (3, 8, 1000),
(8, 1, 10000), (8, 8, 125),
(14, 2, 1), (14, 3, 125), (14, 4, 125), (14, 7, 540),
(17, 4, 50), (17, 7, 100),
(44, 7, 1),
(50, 1, 2025), (50, 7, 9000), (50, 8, 1400),
(70, 5, 4000);

-- Invoices (amount = NET TTC computed by the application: gross - discount + 7% VAT)
INSERT INTO `facture` (`id_facture`, `date_facture`, `montant`, `qte`, `statut`, `id_commande`) VALUES
(1,  '2026-04-11', 5933.91,  2197,  'En attente', 1),
(8,  '2026-04-11', 27289.01, 10125, 'Payée',      8),
(10, '2026-04-22', 6037.58,  2300,  'En attente', 3),
(25, '2026-04-21', 351.50,   150,   'En attente', 17),
(77, '2026-04-18', 2.79,    1,     'Payée', 44);

-- Payments
INSERT INTO `paiement` (`id_paie`, `montant_paie`, `date_paie`, `statut_paie`, `mode_paie`, `id_facture`) VALUES
(1,  500,      '2026-04-11', 'Partiel', 'Espèces',  1),
(14, 2300,     '2026-04-22', 'Partiel', 'Espèces',  10),
(21, 200,      '2026-04-21', 'Partiel', 'Chèque',   25),
(22, 27289.01, '2026-04-12', 'Payé',    'Virement', 8),
(23, 2.79,     '2026-04-18', 'Payé',    'Espèces',  77);

-- Returns / complaints
INSERT INTO `retour` (`id_retour`, `motif`, `etat_produit`, `date`, `id_facture`) VALUES
(1, 'Erreur de livraison', 'Bon état', '2026-04-11', 1),
(5, 'Produit défectueux',  'Bon état', '2026-04-11', 8);

COMMIT;

-- Keep AUTO_INCREMENT counters above the demo identifiers
ALTER TABLE `administration` AUTO_INCREMENT = 2;
ALTER TABLE `client`         AUTO_INCREMENT = 5;
ALTER TABLE `commande`       AUTO_INCREMENT = 71;
ALTER TABLE `depot`          AUTO_INCREMENT = 9;
ALTER TABLE `facture`        AUTO_INCREMENT = 78;
ALTER TABLE `paiement`       AUTO_INCREMENT = 24;
ALTER TABLE `produit`        AUTO_INCREMENT = 9;
ALTER TABLE `retour`         AUTO_INCREMENT = 15;
