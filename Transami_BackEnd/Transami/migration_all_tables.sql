-- Script de migration complet pour ajouter admin_id et contraintes d'unicité
-- À exécuter sur votre base de données

-- ============================================
-- 1. MIGRATION invoice_number_counter
-- ============================================

-- Sauvegarder les données
CREATE TABLE invoice_number_counter_backup AS 
SELECT * FROM invoice_number_counter;

-- Supprimer l'ancienne table
DROP TABLE invoice_number_counter;

-- Créer la nouvelle structure
CREATE TABLE invoice_number_counter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    admin_id BIGINT NOT NULL,
    current_number BIGINT NOT NULL,
    UNIQUE KEY uk_year_admin (year, admin_id)
);

-- ============================================
-- 2. MIGRATION bons_de_livraison
-- ============================================

-- Ajouter la colonne admin_id
ALTER TABLE bons_de_livraison 
ADD COLUMN admin_id BIGINT NOT NULL DEFAULT 1 AFTER id;

-- Mettre à jour admin_id depuis la table camions
UPDATE bons_de_livraison bdl
INNER JOIN camions c ON bdl.camion_id = c.id
SET bdl.admin_id = c.admin_id;

-- Supprimer l'ancienne contrainte unique sur numero
ALTER TABLE bons_de_livraison 
DROP INDEX IF EXISTS UK_numero;

-- Ajouter la nouvelle contrainte unique (numero, admin_id)
ALTER TABLE bons_de_livraison 
ADD UNIQUE KEY uk_numero_admin (numero, admin_id);

-- ============================================
-- 3. MIGRATION factures
-- ============================================

-- Ajouter la colonne admin_id
ALTER TABLE factures 
ADD COLUMN admin_id BIGINT NOT NULL DEFAULT 1 AFTER id;

-- Mettre à jour admin_id depuis la table clients
UPDATE factures f
INNER JOIN clients c ON f.client_id = c.id
SET f.admin_id = c.admin_id;

-- Supprimer l'ancienne contrainte unique sur numero
ALTER TABLE factures 
DROP INDEX IF EXISTS UK_numero;

-- Ajouter la nouvelle contrainte unique (numero, admin_id)
ALTER TABLE factures 
ADD UNIQUE KEY uk_numero_admin (numero, admin_id);

-- ============================================
-- 4. MIGRATION codes_produit
-- ============================================

-- Ajouter la colonne admin_id (par défaut 1 pour les données existantes)
ALTER TABLE codes_produit 
ADD COLUMN admin_id BIGINT NOT NULL DEFAULT 1 AFTER id;

-- Supprimer l'ancienne contrainte unique sur code
ALTER TABLE codes_produit 
DROP INDEX IF EXISTS UK_code;

-- Ajouter la nouvelle contrainte unique (code, admin_id)
ALTER TABLE codes_produit 
ADD UNIQUE KEY uk_code_admin (code, admin_id);

-- ============================================
-- 5. MIGRATION camions
-- ============================================

-- Supprimer l'ancienne contrainte unique sur matricule
ALTER TABLE camions 
DROP INDEX IF EXISTS UK_matricule;

-- Ajouter la nouvelle contrainte unique (matricule, admin_id)
ALTER TABLE camions 
ADD UNIQUE KEY uk_matricule_admin (matricule, admin_id);

-- ============================================
-- 6. MIGRATION clients
-- ============================================

-- Ajouter la contrainte unique (nom, admin_id)
ALTER TABLE clients 
ADD UNIQUE KEY uk_nom_admin (nom, admin_id);

-- ============================================
-- 7. MIGRATION fournisseurs
-- ============================================

-- Ajouter la contrainte unique (nom, admin_id)
ALTER TABLE fournisseurs 
ADD UNIQUE KEY uk_nom_admin (nom, admin_id);

-- ============================================
-- VÉRIFICATION
-- ============================================

-- Vérifier les contraintes
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE
FROM 
    INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN (
        'bons_de_livraison',
        'factures',
        'codes_produit',
        'camions',
        'clients',
        'fournisseurs',
        'invoice_number_counter'
    )
ORDER BY 
    TABLE_NAME, CONSTRAINT_TYPE;
