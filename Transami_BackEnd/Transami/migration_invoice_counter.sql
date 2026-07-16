-- Migration script pour invoice_number_counter
-- À exécuter sur votre base de données

-- 1. Sauvegarder les données existantes
CREATE TABLE invoice_number_counter_backup AS 
SELECT * FROM invoice_number_counter;

-- 2. Supprimer l'ancienne table
DROP TABLE invoice_number_counter;

-- 3. Créer la nouvelle table avec la bonne structure
CREATE TABLE invoice_number_counter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    admin_id BIGINT NOT NULL,
    current_number BIGINT NOT NULL,
    UNIQUE KEY uk_year_admin (year, admin_id)
);

-- 4. Restaurer les données (si vous aviez des données)
-- Note: Vous devrez ajouter admin_id manuellement pour chaque ligne
-- INSERT INTO invoice_number_counter (year, admin_id, current_number)
-- SELECT year, 1, current_number FROM invoice_number_counter_backup;

-- 5. Supprimer la sauvegarde (optionnel)
-- DROP TABLE invoice_number_counter_backup;
