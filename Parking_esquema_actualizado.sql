-- MySQL dump 10.13  Distrib 9.5.0, for Win64 (x86_64)
--
-- Host: localhost    Database: parking
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '076e7053-d302-11f0-b154-74563c95fccd:1-544';

--
-- Table structure for table `alumnos`
--

DROP TABLE IF EXISTS `alumnos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alumnos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `id_curso` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cajamaster`
--

DROP TABLE IF EXISTS `cajamaster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cajamaster` (
  `CajaId` varchar(30) NOT NULL,
  `CentroComercialId` int NOT NULL,
  `TipoCaja` varchar(50) DEFAULT NULL,
  `Ubicacion` varchar(100) DEFAULT NULL,
  `Nivel` varchar(3) DEFAULT NULL,
  `PlanoX` decimal(5,2) DEFAULT NULL,
  `PlanoY` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`CajaId`),
  KEY `CentroComercialId` (`CentroComercialId`),
  CONSTRAINT `cajamaster_ibfk_1` FOREIGN KEY (`CentroComercialId`) REFERENCES `centrocomercial` (`CentroComercialId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `centrocomercial`
--

DROP TABLE IF EXISTS `centrocomercial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `centrocomercial` (
  `CentroComercialId` int NOT NULL,
  `Nombre` varchar(150) NOT NULL,
  `Direccion` varchar(250) DEFAULT NULL,
  `Estado` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`CentroComercialId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cursos`
--

DROP TABLE IF EXISTS `cursos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cursos` (
  `id` int NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `vacantes` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entrada`
--

DROP TABLE IF EXISTS `entrada`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `entrada` (
  `ParkingTransNo` bigint NOT NULL AUTO_INCREMENT,
  `CentroComercialId` int NOT NULL,
  `CardNo` varchar(15) NOT NULL,
  `PuertaId` int NOT NULL,
  `ArticuloId` int DEFAULT NULL,
  `FechaHora` datetime NOT NULL,
  `Puerta` varchar(80) DEFAULT NULL,
  `Articulo` varchar(12) DEFAULT NULL,
  `MinutosPermanencia` int DEFAULT NULL,
  `TipoClienteId` int DEFAULT NULL,
  `TipoTarjetaId` varchar(250) DEFAULT NULL,
  `NumeroTarjeta` bigint DEFAULT NULL,
  `UsuarioId` int DEFAULT NULL,
  `TipoZonaId` int DEFAULT NULL,
  `ZonaId` int DEFAULT NULL,
  `Zona` int DEFAULT NULL,
  `Placa` char(7) DEFAULT NULL,
  `TienePlaca` int NOT NULL DEFAULT '0',
  `TieneSalida` int NOT NULL DEFAULT '0',
  `TieneVenta` int NOT NULL DEFAULT '0',
  `TieneValidador` int NOT NULL DEFAULT '0',
  `FlagManual` tinyint(1) NOT NULL DEFAULT '0',
  `NumeroTarifaTicket` varchar(100) DEFAULT NULL,
  `PlacaActiva` varchar(7) GENERATED ALWAYS AS ((case when ((`TieneSalida` = 0) and (`Placa` is not null) and (trim(`Placa`) <> _utf8mb4'')) then upper(trim(`Placa`)) else NULL end)) STORED,
  PRIMARY KEY (`ParkingTransNo`),
  UNIQUE KEY `uq_entrada_placa_activa` (`PlacaActiva`),
  KEY `CentroComercialId` (`CentroComercialId`),
  KEY `PuertaId` (`PuertaId`),
  KEY `ZonaId` (`ZonaId`),
  CONSTRAINT `entrada_ibfk_1` FOREIGN KEY (`CentroComercialId`) REFERENCES `centrocomercial` (`CentroComercialId`),
  CONSTRAINT `entrada_ibfk_2` FOREIGN KEY (`PuertaId`) REFERENCES `puertamaster` (`PuertaId`),
  CONSTRAINT `entrada_ibfk_3` FOREIGN KEY (`ZonaId`) REFERENCES `zona` (`ZonaId`)
) ENGINE=InnoDB AUTO_INCREMENT=1013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `puertamaster`
--

DROP TABLE IF EXISTS `puertamaster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `puertamaster` (
  `PuertaId` int NOT NULL,
  `CentroComercialId` int NOT NULL,
  `Descripcion` varchar(100) NOT NULL,
  `Tipo` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`PuertaId`),
  KEY `CentroComercialId` (`CentroComercialId`),
  CONSTRAINT `puertamaster_ibfk_1` FOREIGN KEY (`CentroComercialId`) REFERENCES `centrocomercial` (`CentroComercialId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `RolId` int NOT NULL AUTO_INCREMENT,
  `NombreRol` varchar(50) NOT NULL,
  `NivelAcceso` int DEFAULT NULL,
  PRIMARY KEY (`RolId`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `salida`
--

DROP TABLE IF EXISTS `salida`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salida` (
  `ParkingTransNo` bigint NOT NULL AUTO_INCREMENT,
  `EntradaParkingTransNo` bigint DEFAULT NULL,
  `CentroComercialId` int NOT NULL,
  `CardNo` varchar(15) NOT NULL,
  `PuertaId` int DEFAULT NULL,
  `ArticuloId` int DEFAULT NULL,
  `FechaHora` datetime DEFAULT NULL,
  `Puerta` varchar(80) DEFAULT NULL,
  `Articulo` varchar(12) DEFAULT NULL,
  `MinutosPermanencia` int DEFAULT NULL,
  `TipoClienteId` int DEFAULT NULL,
  `TipoTarjetaId` varchar(250) DEFAULT NULL,
  `NumeroTarjeta` bigint DEFAULT NULL,
  `UsuarioId` int DEFAULT NULL,
  `TipoZonaId` int DEFAULT NULL,
  `ZonaId` int DEFAULT NULL,
  `Zona` int DEFAULT NULL,
  `Placa` char(7) DEFAULT NULL,
  `TienePlaca` int NOT NULL DEFAULT '0',
  `TieneEntrada` int NOT NULL DEFAULT '0',
  `TieneVenta` int NOT NULL DEFAULT '0',
  `FlagManual` tinyint(1) NOT NULL DEFAULT '0',
  `NumeroTarifaTicket` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ParkingTransNo`),
  KEY `CentroComercialId` (`CentroComercialId`),
  KEY `PuertaId` (`PuertaId`),
  KEY `ZonaId` (`ZonaId`),
  CONSTRAINT `salida_ibfk_1` FOREIGN KEY (`CentroComercialId`) REFERENCES `centrocomercial` (`CentroComercialId`),
  CONSTRAINT `salida_ibfk_2` FOREIGN KEY (`PuertaId`) REFERENCES `puertamaster` (`PuertaId`),
  CONSTRAINT `salida_ibfk_3` FOREIGN KEY (`ZonaId`) REFERENCES `zona` (`ZonaId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuariosistema`
--

DROP TABLE IF EXISTS `usuariosistema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuariosistema` (
  `UsuarioId` int NOT NULL AUTO_INCREMENT,
  `Username` varchar(50) NOT NULL,
  `PasswordHash` varchar(256) NOT NULL,
  `Nombres` varchar(150) NOT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `RolId` int NOT NULL,
  `CentroComercialId` int DEFAULT NULL,
  `FechaCreacion` datetime DEFAULT CURRENT_TIMESTAMP,
  `Activo` tinyint(1) DEFAULT '1',
  `FotoPerfil` varchar(100) DEFAULT NULL,
  `Documento` varchar(20) DEFAULT NULL,
  `Telefono` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`UsuarioId`),
  UNIQUE KEY `Username` (`Username`),
  KEY `RolId` (`RolId`),
  KEY `CentroComercialId` (`CentroComercialId`),
  CONSTRAINT `usuariosistema_ibfk_1` FOREIGN KEY (`RolId`) REFERENCES `rol` (`RolId`),
  CONSTRAINT `usuariosistema_ibfk_2` FOREIGN KEY (`CentroComercialId`) REFERENCES `centrocomercial` (`CentroComercialId`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `venta`
--

DROP TABLE IF EXISTS `venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `venta` (
  `TransaccionNo` int NOT NULL,
  `CentroComercialId` int NOT NULL,
  `CajaId` varchar(30) NOT NULL,
  `ArticuloId` int NOT NULL,
  `FechaHora` datetime NOT NULL,
  `CardNo` varchar(15) DEFAULT NULL,
  `CardNoTo` varchar(15) DEFAULT NULL,
  `Caja` varchar(150) NOT NULL,
  `TipoCajaId` varchar(50) NOT NULL,
  `Articulo` varchar(30) NOT NULL,
  `CategoriaArticulo` varchar(30) NOT NULL,
  `TipoVentaId` varchar(30) NOT NULL,
  `Cantidad` int NOT NULL,
  `MontoTotal` decimal(19,2) DEFAULT NULL,
  `MontoNeto` decimal(18,2) NOT NULL,
  `NumeroDocFiscal` int NOT NULL,
  `OperadorId` int DEFAULT NULL,
  `Operador` varchar(152) NOT NULL,
  `ValidoDesde` datetime DEFAULT NULL,
  `ValidoHasta` datetime DEFAULT NULL,
  `Beneficiario` varchar(1) NOT NULL,
  `TieneEntrada` int NOT NULL,
  `TieneSalida` int NOT NULL,
  `ParkingTransNo` bigint DEFAULT NULL,
  `TipoDocumento` varchar(25) NOT NULL,
  `NumeroDocumentoEPos` varchar(30) DEFAULT NULL,
  `FLAG_DOCUMENTO_ANULADO` tinyint(1) NOT NULL,
  `FormaPago` varchar(50) DEFAULT NULL,
  `Descripcion` varchar(250) NOT NULL,
  `NumeroTarifaTicket` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`TransaccionNo`),
  KEY `CentroComercialId` (`CentroComercialId`),
  KEY `CajaId` (`CajaId`),
  KEY `OperadorId` (`OperadorId`),
  CONSTRAINT `venta_ibfk_1` FOREIGN KEY (`CentroComercialId`) REFERENCES `centrocomercial` (`CentroComercialId`),
  CONSTRAINT `venta_ibfk_2` FOREIGN KEY (`CajaId`) REFERENCES `cajamaster` (`CajaId`),
  CONSTRAINT `venta_ibfk_3` FOREIGN KEY (`OperadorId`) REFERENCES `usuariosistema` (`UsuarioId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `zona`
--

DROP TABLE IF EXISTS `zona`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `zona` (
  `ZonaId` int NOT NULL AUTO_INCREMENT,
  `CentroComercialId` int NOT NULL,
  `Nombre` varchar(100) NOT NULL,
  `Ubicacion` varchar(150) DEFAULT NULL,
  `CapacidadTotal` int NOT NULL,
  `TarifaHora` decimal(6,2) NOT NULL DEFAULT '0.00',
  `Nivel` varchar(3) DEFAULT NULL,
  `PlanoX` decimal(5,2) DEFAULT NULL,
  `PlanoY` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`ZonaId`),
  KEY `CentroComercialId` (`CentroComercialId`),
  CONSTRAINT `zona_ibfk_1` FOREIGN KEY (`CentroComercialId`) REFERENCES `centrocomercial` (`CentroComercialId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-22 18:42:15
