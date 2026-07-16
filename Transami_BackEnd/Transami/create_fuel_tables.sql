-- Script de création des tables pour le module de gestion du carburant
-- À exécuter sur votre base de données

-- ============================================
-- 1. TABLE stations
-- ============================================
CREATE TABLE IF NOT EXISTS stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    localisation VARCHAR(255) NOT NULL,
    admin_id BIGINT NOT NULL,
    total_annuelle DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    total_mensuelle DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    total_diesel_mois DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    total_diesel50_mois DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    total_essence_mois DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    UNIQUE KEY uk_nom_admin (nom, admin_id),
    INDEX idx_admin_id (admin_id)
);

-- ============================================
-- 2. TABLE prix_carburant
-- ============================================
CREATE TABLE IF NOT EXISTS prix_carburant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL UNIQUE,
    prix_essence DECIMAL(10, 3) NOT NULL,
    prix_diesel DECIMAL(10, 3) NOT NULL,
    prix_diesel50 DECIMAL(10, 3) NOT NULL,
    UNIQUE KEY uk_admin_id (admin_id)
);

-- ============================================
-- 3. TABLE bons_carburant
-- ============================================
CREATE TABLE IF NOT EXISTS bons_carburant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    date DATE NOT NULL,
    camion_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    kilometrage_actuel DOUBLE NOT NULL,
    quantite_litres DOUBLE NOT NULL,
    typ_carburant VARCHAR(50) NOT NULL,
    prix_litre DECIMAL(10, 3) NOT NULL,
    montant_total DECIMAL(19, 2) NOT NULL,
    distance_parcourue DOUBLE,
    consommation_reelle DOUBLE,
    commentaire VARCHAR(500),
    INDEX idx_admin_id (admin_id),
    INDEX idx_camion_id (camion_id),
    INDEX idx_station_id (station_id),
    INDEX idx_date (date),
    FOREIGN KEY (camion_id) REFERENCES camions(id) ON DELETE CASCADE,
    FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE
);

-- ============================================
-- VÉRIFICATION
-- ============================================
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_KEY
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN ('stations', 'prix_carburant', 'bons_carburant')
ORDER BY 
    TABLE_NAME, ORDINAL_POSITION;
