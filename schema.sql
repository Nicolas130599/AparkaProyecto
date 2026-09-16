CREATE DATABASE IF NOT EXISTS aparka_db;
USE aparka_db;

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    placa VARCHAR(15),
    rol VARCHAR(10) NOT NULL DEFAULT 'USUARIO'
);

CREATE TABLE zona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150) NOT NULL,
    capacidad_total INT NOT NULL,
    capacidad_ocupada INT NOT NULL DEFAULT 0,
    tarifa_hora DECIMAL(6,2) NOT NULL DEFAULT 0
);

CREATE TABLE reserva (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    zona_id INT NOT NULL,
    placa VARCHAR(15) NOT NULL,
    fecha_ingreso DATETIME NOT NULL,
    fecha_salida DATETIME NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (zona_id) REFERENCES zona(id)
);

-- Usuario administrador de prueba (cámbialo en producción)
INSERT INTO usuario (nombre, correo, contrasena, placa, rol)
VALUES ('Admin Aparka', 'admin@aparka.com', 'admin123', NULL, 'ADMIN');

-- Usuario de prueba
INSERT INTO usuario (nombre, correo, contrasena, placa, rol)
VALUES ('Usuario Demo', 'demo@aparka.com', 'demo123', 'XYZ-987', 'USUARIO');

-- Zonas de ejemplo con distintos niveles de flujo vehicular
INSERT INTO zona (nombre, ubicacion, capacidad_total, capacidad_ocupada, tarifa_hora) VALUES
('Zona Centro',      'Av. Principal 123',   50, 44, 4.50),
('Zona Norte',       'Jr. Los Pinos 456',   30, 12, 3.00),
('Zona Universidad', 'Av. Universitaria 789', 80, 65, 3.50),
('Zona Mercado',     'Calle Comercio 321',  25,  9, 2.50);
