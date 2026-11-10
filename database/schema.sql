-- =============================================================================
-- SOKTRAPA - Sales Management Information System
-- Database schema (structure only)
--
-- Target  : MySQL 8.x / MariaDB 10.4+
-- Source  : derived from the original phpMyAdmin export of the `soktrapa` database
--           (tables, column types, primary keys and indexes unchanged).
--
-- Delete rules (changed from the original export, where every foreign key was
-- ON DELETE CASCADE - see docs/database-design.md, "Delete safety"):
--   RESTRICT : commande -> client, ligne_commande -> commande / produit,
--              facture -> commande, paiement -> facture, retour -> facture
--              A customer, order, product or invoice that is still referenced
--              cannot be deleted, so business and financial records are never
--              removed silently and reserved stock cannot be lost.
--   CASCADE  : stocker -> produit / depot (pure association rows).
-- Existing databases: apply database/migrations/001_restrict_deletes.sql.
-- Usage   : mysql -u <user> -p < database/schema.sql
--           then:  mysql -u <user> -p soktrapa < database/sample_data.sql
--
-- This script is non-destructive: it never drops a database or a table.
-- It fails on purpose if the tables already exist.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `soktrapa`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `soktrapa`;

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- Application administrators (login screen)
-- `password` stores a PBKDF2 hash (see projet.PasswordHasher). VARCHAR(200) is
-- large enough for the hash format, so the original column definition is kept.
-- -----------------------------------------------------------------------------
CREATE TABLE `administration` (
  `id`              int(11)      NOT NULL AUTO_INCREMENT,
  `nom_utilisateur` varchar(200) NOT NULL,
  `password`        varchar(200) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Customers
-- -----------------------------------------------------------------------------
CREATE TABLE `client` (
  `id_client`          int(11)      NOT NULL AUTO_INCREMENT,
  `nom_client`         varchar(255) DEFAULT NULL,
  `prenom_client`      varchar(255) DEFAULT NULL,
  `adress_client`      varchar(200) NOT NULL,
  `tel_client`         varchar(50)  DEFAULT NULL,
  `email_client`       varchar(255) DEFAULT NULL,
  `type_client`        varchar(50)  DEFAULT NULL,
  `condition_paiement` varchar(100) DEFAULT NULL,
  `plafond_credit`     double       DEFAULT NULL,
  PRIMARY KEY (`id_client`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Products (finished goods)
-- -----------------------------------------------------------------------------
CREATE TABLE `produit` (
  `id_produit`   int(11)      NOT NULL AUTO_INCREMENT,
  `nom_produit`  varchar(255) DEFAULT NULL,
  `prix_produit` double       DEFAULT NULL,
  `type_produit` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_produit`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Storage locations / lots with available quantity
-- -----------------------------------------------------------------------------
CREATE TABLE `depot` (
  `id_depot`        int(11)      NOT NULL AUTO_INCREMENT,
  `lot_depot`       varchar(100) DEFAULT NULL,
  `lieu_depot`      varchar(255) DEFAULT NULL,
  `mouvement_depot` varchar(100) DEFAULT NULL,
  `qte_disponible`  int(11)      DEFAULT NULL,
  PRIMARY KEY (`id_depot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Association product <-> depot (many-to-many)
-- -----------------------------------------------------------------------------
CREATE TABLE `stocker` (
  `id_produit` int(11) NOT NULL,
  `id_depot`   int(11) NOT NULL,
  PRIMARY KEY (`id_produit`, `id_depot`),
  KEY `id_depot` (`id_depot`),
  CONSTRAINT `stocker_ibfk_1` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `stocker_ibfk_2` FOREIGN KEY (`id_depot`) REFERENCES `depot` (`id_depot`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Sales orders
-- -----------------------------------------------------------------------------
CREATE TABLE `commande` (
  `id_commande`   int(11)     NOT NULL AUTO_INCREMENT,
  `date_commande` date        DEFAULT NULL,
  `statut`        varchar(50) DEFAULT NULL,
  `date_prevu`    date        DEFAULT NULL,
  `remise`        double      DEFAULT NULL,
  `id_client`     int(11)     DEFAULT NULL,
  PRIMARY KEY (`id_commande`),
  KEY `id_client` (`id_client`),
  CONSTRAINT `commande_ibfk_1` FOREIGN KEY (`id_client`) REFERENCES `client` (`id_client`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Order lines (many-to-many order <-> product, with quantity)
-- -----------------------------------------------------------------------------
CREATE TABLE `ligne_commande` (
  `id_commande`  int(11) NOT NULL,
  `id_produit`   int(11) NOT NULL,
  `qte_commande` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_commande`, `id_produit`),
  KEY `id_produit` (`id_produit`),
  CONSTRAINT `ligne_commande_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_commande`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ligne_commande_ibfk_2` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Invoices (one order may have several invoices: no UNIQUE on id_commande)
-- -----------------------------------------------------------------------------
CREATE TABLE `facture` (
  `id_facture`   int(11)     NOT NULL AUTO_INCREMENT,
  `date_facture` date        DEFAULT NULL,
  `montant`      double      DEFAULT NULL,
  `qte`          int(11)     DEFAULT NULL,
  `statut`       varchar(50) DEFAULT NULL,
  `id_commande`  int(11)     DEFAULT NULL,
  PRIMARY KEY (`id_facture`),
  KEY `id_commande` (`id_commande`),
  CONSTRAINT `facture_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_commande`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Customer payments
-- -----------------------------------------------------------------------------
CREATE TABLE `paiement` (
  `id_paie`     int(11)     NOT NULL AUTO_INCREMENT,
  `montant_paie` double     DEFAULT NULL,
  `date_paie`   date        DEFAULT NULL,
  `statut_paie` varchar(50) DEFAULT NULL,
  `mode_paie`   varchar(50) DEFAULT NULL,
  `id_facture`  int(11)     DEFAULT NULL,
  PRIMARY KEY (`id_paie`),
  KEY `id_facture` (`id_facture`),
  CONSTRAINT `paiement_ibfk_1` FOREIGN KEY (`id_facture`) REFERENCES `facture` (`id_facture`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------------------------
-- Product returns / complaints
-- -----------------------------------------------------------------------------
CREATE TABLE `retour` (
  `id_retour`    int(11)      NOT NULL AUTO_INCREMENT,
  `motif`        varchar(255) DEFAULT NULL,
  `etat_produit` varchar(100) DEFAULT NULL,
  `date`         date         DEFAULT NULL,
  `id_facture`   int(11)      DEFAULT NULL,
  PRIMARY KEY (`id_retour`),
  KEY `id_facture` (`id_facture`),
  CONSTRAINT `retour_ibfk_1` FOREIGN KEY (`id_facture`) REFERENCES `facture` (`id_facture`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
