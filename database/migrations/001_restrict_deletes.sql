-- =============================================================================
-- Migration 001 - replace ON DELETE CASCADE with ON DELETE RESTRICT
--
-- For databases created BEFORE this change (original export, or schema.sql of the
-- first published version). A database created with the current schema.sql already
-- has these rules and does not need this script.
--
--   mysql -u <admin user> -p soktrapa < database/migrations/001_restrict_deletes.sql
--
-- Requires the ALTER privilege (run it with an administrative account, not with the
-- least-privilege application account). No row is modified or deleted.
-- Re-running it is harmless: it recreates the same six constraints.
-- =============================================================================

USE `soktrapa`;

ALTER TABLE `commande`
  DROP FOREIGN KEY `commande_ibfk_1`;
ALTER TABLE `commande`
  ADD CONSTRAINT `commande_ibfk_1` FOREIGN KEY (`id_client`) REFERENCES `client` (`id_client`)
    ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `ligne_commande`
  DROP FOREIGN KEY `ligne_commande_ibfk_1`,
  DROP FOREIGN KEY `ligne_commande_ibfk_2`;
ALTER TABLE `ligne_commande`
  ADD CONSTRAINT `ligne_commande_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_commande`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `ligne_commande_ibfk_2` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`)
    ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `facture`
  DROP FOREIGN KEY `facture_ibfk_1`;
ALTER TABLE `facture`
  ADD CONSTRAINT `facture_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_commande`)
    ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `paiement`
  DROP FOREIGN KEY `paiement_ibfk_1`;
ALTER TABLE `paiement`
  ADD CONSTRAINT `paiement_ibfk_1` FOREIGN KEY (`id_facture`) REFERENCES `facture` (`id_facture`)
    ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `retour`
  DROP FOREIGN KEY `retour_ibfk_1`;
ALTER TABLE `retour`
  ADD CONSTRAINT `retour_ibfk_1` FOREIGN KEY (`id_facture`) REFERENCES `facture` (`id_facture`)
    ON DELETE RESTRICT ON UPDATE CASCADE;
