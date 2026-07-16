-- Script pour recréer toutes les tables avec la nouvelle structure
-- ATTENTION: Ce script supprime toutes les données existantes !
-- Utilisez migration_all_tables.sql si vous voulez conserver vos données

-- ============================================
-- DÉSACTIVER LES CONTRAINTES DE CLÉ ÉTRANGÈRE
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- SUPPRIMER LES TABLES DANS L'ORDRE
-- ============================================
DROP TABLE IF EXISTS bons_de_livraison;
DROP TABLE IF EXISTS factures;
DROP TABLE IF EXISTS codes_produit;
DROP TABLE IF EXISTS camions;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS fournisseurs;
DROP TABLE IF EXISTS invoice_number_counter;
DROP TABLE IF EXISTS admin_telephones;
DROP TABLE IF EXISTS admins;

-- ============================================
-- RÉACTIVER LES CONTRAINTES
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- LAISSER HIBERNATE RECRÉER LES TABLES
-- ============================================
-- Après avoir exécuté ce script, redémarrez votre application Spring Boot
-- avec spring.jpa.hibernate.ddl-auto=update ou create
-- Hibernate créera automatiquement les tables avec la bonne structure
