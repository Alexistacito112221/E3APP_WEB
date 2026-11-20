-- Ejecuta este script UNA vez en MySQL antes de usar la API.
-- (Ej.: en MySQL Workbench, o en terminal: mysql -u root -p < sql/schema.sql)
-- Solo crea la base de datos; la tabla la crea Django con "python manage.py migrate".

CREATE DATABASE IF NOT EXISTS catalogo_videojuegos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Opcional: un usuario propio en lugar de usar root.
-- CREATE USER 'catalogo'@'localhost' IDENTIFIED BY 'cambia_esta_clave';
-- GRANT ALL PRIVILEGES ON catalogo_videojuegos.* TO 'catalogo'@'localhost';
-- FLUSH PRIVILEGES;

-- Referencia: así queda la tabla que crea Django (NO la ejecutes antes de migrate).
-- CREATE TABLE videojuegos (
--     id INT AUTO_INCREMENT PRIMARY KEY,
--     nombre VARCHAR(150) NOT NULL,
--     descripcion LONGTEXT NOT NULL,
--     precio_promedio DECIMAL(8,2) NOT NULL,
--     plataforma VARCHAR(60) NOT NULL
-- );
