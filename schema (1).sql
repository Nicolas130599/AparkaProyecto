
CREATE DATABASE IF NOT EXISTS Parking;
USE Parking;

CREATE TABLE CentroComercial (
    CentroComercialId INT NOT NULL PRIMARY KEY,
    Nombre VARCHAR(150) NOT NULL,
    Direccion VARCHAR(250) NULL,
    Estado TINYINT(1) DEFAULT 1
);

CREATE TABLE Rol (
    RolId INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    NombreRol VARCHAR(50) NOT NULL,
    NivelAcceso INT NULL
);

CREATE TABLE UsuarioSistema (
    UsuarioId INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) NOT NULL UNIQUE,
    PasswordHash VARCHAR(256) NOT NULL,
    Nombres VARCHAR(150) NOT NULL,
    Email VARCHAR(100) NULL,
    RolId INT NOT NULL,
    CentroComercialId INT NULL,
    FechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    Activo TINYINT(1) DEFAULT 1,
    FOREIGN KEY (RolId) REFERENCES Rol(RolId),
    FOREIGN KEY (CentroComercialId) REFERENCES CentroComercial(CentroComercialId)
);

CREATE TABLE CajaMaster (
    CajaId VARCHAR(30) NOT NULL PRIMARY KEY,
    CentroComercialId INT NOT NULL,
    TipoCaja VARCHAR(50) NULL,
    Ubicacion VARCHAR(100) NULL,
    FOREIGN KEY (CentroComercialId) REFERENCES CentroComercial(CentroComercialId)
);

CREATE TABLE PuertaMaster (
    PuertaId INT NOT NULL PRIMARY KEY,
    CentroComercialId INT NOT NULL,
    Descripcion VARCHAR(100) NOT NULL,
    Tipo VARCHAR(50) NULL,
    FOREIGN KEY (CentroComercialId) REFERENCES CentroComercial(CentroComercialId)
);


CREATE TABLE Zona (
    ZonaId INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    CentroComercialId INT NOT NULL,
    Nombre VARCHAR(100) NOT NULL,
    Ubicacion VARCHAR(150) NULL,
    CapacidadTotal INT NOT NULL,
    TarifaHora DECIMAL(6,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (CentroComercialId) REFERENCES CentroComercial(CentroComercialId)
);


CREATE TABLE Venta (
    TransaccionNo INT NOT NULL PRIMARY KEY,
    CentroComercialId INT NOT NULL,
    CajaId VARCHAR(30) NOT NULL,
    ArticuloId INT NOT NULL,
    FechaHora DATETIME NOT NULL,
    CardNo VARCHAR(15) NULL,
    CardNoTo VARCHAR(15) NULL,
    Caja VARCHAR(150) NOT NULL,
    TipoCajaId VARCHAR(50) NOT NULL,
    Articulo VARCHAR(30) NOT NULL,
    CategoriaArticulo VARCHAR(30) NOT NULL,
    TipoVentaId VARCHAR(30) NOT NULL,
    Cantidad INT NOT NULL,
    MontoTotal DECIMAL(19,2) NULL,
    MontoNeto DECIMAL(18,2) NOT NULL,
    NumeroDocFiscal INT NOT NULL,
    OperadorId INT NULL,
    Operador VARCHAR(152) NOT NULL,
    ValidoDesde DATETIME NULL,
    ValidoHasta DATETIME NULL,
    Beneficiario VARCHAR(1) NOT NULL,
    TieneEntrada INT NOT NULL,
    TieneSalida INT NOT NULL,
    ParkingTransNo BIGINT NULL,
    TipoDocumento VARCHAR(25) NOT NULL,
    NumeroDocumentoEPos VARCHAR(30) NULL,
    FLAG_DOCUMENTO_ANULADO TINYINT(1) NOT NULL,
    FormaPago VARCHAR(50) NULL,
    Descripcion VARCHAR(250) NOT NULL,
    NumeroTarifaTicket VARCHAR(100) NULL,
    FOREIGN KEY (CentroComercialId) REFERENCES CentroComercial(CentroComercialId),
    FOREIGN KEY (CajaId) REFERENCES CajaMaster(CajaId),
    FOREIGN KEY (OperadorId) REFERENCES UsuarioSistema(UsuarioId)
);

CREATE TABLE Entrada (
    ParkingTransNo BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    CentroComercialId INT NOT NULL,
    CardNo VARCHAR(15) NOT NULL,
    PuertaId INT NOT NULL,
    ArticuloId INT NULL,
    FechaHora DATETIME NOT NULL,
    Puerta VARCHAR(80) NULL,
    Articulo VARCHAR(12) NULL,
    MinutosPermanencia INT NULL,
    TipoClienteId INT NULL,
    TipoTarjetaId VARCHAR(250) NULL,
    NumeroTarjeta BIGINT NULL,
    UsuarioId INT NULL,
    TipoZonaId INT NULL,
    ZonaId INT NULL,
    Zona INT NULL,
    Placa CHAR(7) NULL,
    TienePlaca INT NOT NULL DEFAULT 0,
    TieneSalida INT NOT NULL DEFAULT 0,
    TieneVenta INT NOT NULL DEFAULT 0,
    TieneValidador INT NOT NULL DEFAULT 0,
    FlagManual TINYINT(1) NOT NULL DEFAULT 0,
    NumeroTarifaTicket VARCHAR(100) NULL,
    FOREIGN KEY (CentroComercialId) REFERENCES CentroComercial(CentroComercialId),
    FOREIGN KEY (PuertaId) REFERENCES PuertaMaster(PuertaId),
    FOREIGN KEY (ZonaId) REFERENCES Zona(ZonaId)
);

CREATE TABLE Salida (
    ParkingTransNo BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    EntradaParkingTransNo BIGINT NULL,
    CentroComercialId INT NOT NULL,
    CardNo VARCHAR(15) NOT NULL,
    PuertaId INT NULL,
    ArticuloId INT NULL,
    FechaHora DATETIME NULL,
    Puerta VARCHAR(80) NULL,
    Articulo VARCHAR(12) NULL,
    MinutosPermanencia INT NULL,
    TipoClienteId INT NULL,
    TipoTarjetaId VARCHAR(250) NULL,
    NumeroTarjeta BIGINT NULL,
    UsuarioId INT NULL,
    TipoZonaId INT NULL,
    ZonaId INT NULL,
    Zona INT NULL,
    Placa CHAR(7) NULL,
    TienePlaca INT NOT NULL DEFAULT 0,
    TieneEntrada INT NOT NULL DEFAULT 0,
    TieneVenta INT NOT NULL DEFAULT 0,
    FlagManual TINYINT(1) NOT NULL DEFAULT 0,
    NumeroTarifaTicket VARCHAR(100) NULL,
    FOREIGN KEY (CentroComercialId) REFERENCES CentroComercial(CentroComercialId),
    FOREIGN KEY (PuertaId) REFERENCES PuertaMaster(PuertaId),
    FOREIGN KEY (ZonaId) REFERENCES Zona(ZonaId)
);

-- ==========================================
-- PRUEBA
-- ==========================================

INSERT INTO CentroComercial (CentroComercialId, Nombre, Direccion, Estado) VALUES
(1, 'Aparka Plaza Central', 'Av. Principal 123', 1);

INSERT INTO Rol (NombreRol, NivelAcceso) VALUES
('ADMIN', 10),
('USUARIO', 1);

-- Contraseñas en texto plano por simplicidad académica 
INSERT INTO UsuarioSistema (Username, PasswordHash, Nombres, Email, RolId, CentroComercialId) VALUES
('admin', 'admin123', 'Admin Aparka', 'admin@aparka.com', 1, 1),
('demo', 'demo123', 'Usuario Demo', 'demo@aparka.com', 2, 1);

INSERT INTO PuertaMaster (PuertaId, CentroComercialId, Descripcion, Tipo) VALUES
(1, 1, 'Puerta Principal', 'ENTRADA_SALIDA'),
(2, 1, 'Puerta Norte', 'ENTRADA_SALIDA');

INSERT INTO Zona (CentroComercialId, Nombre, Ubicacion, CapacidadTotal, TarifaHora) VALUES
(1, 'Zona Centro', 'Av. Principal 123', 50, 4.50),
(1, 'Zona Norte', 'Jr. Los Pinos 456', 30, 3.00),
(1, 'Zona Universidad', 'Av. Universitaria 789', 80, 3.50),
(1, 'Zona Mercado', 'Calle Comercio 321', 25, 2.50);

-- Algunos ingresos de ejemplo aún sin salida 
INSERT INTO Entrada (CentroComercialId, CardNo, PuertaId, FechaHora, ZonaId, Zona, Placa, TienePlaca, TieneSalida) VALUES
(1, 'C0001', 1, NOW(), 1, 1, 'ABC-111', 1, 0),
(1, 'C0002', 1, NOW(), 1, 1, 'ABC-112', 1, 0),
(1, 'C0003', 2, NOW(), 3, 3, 'XYZ-201', 1, 0);
