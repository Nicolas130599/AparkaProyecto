package com.aparka.dao;

import com.aparka.model.Entrada;
import com.aparka.util.ConexionBD;

import java.sql.*;

public class SalidaDAO {

    /**
     * Registra la salida de un vehículo: inserta la fila en Salida (referenciando
     * la Entrada original) y marca esa Entrada como cerrada (TieneSalida = 1).
     */
    public boolean registrarSalida(Entrada entradaOriginal, int puertaSalidaId) throws SQLException {
        String sqlInsert = "INSERT INTO Salida (EntradaParkingTransNo, CentroComercialId, CardNo, PuertaId, FechaHora, " +
                "UsuarioId, ZonaId, Zona, Placa, TienePlaca, TieneEntrada) " +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?, 1, 1)";

        String sqlCerrarEntrada = "UPDATE Entrada SET TieneSalida = 1 WHERE ParkingTransNo = ?";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                    ps.setLong(1, entradaOriginal.getParkingTransNo());
                    ps.setInt(2, entradaOriginal.getCentroComercialId());
                    ps.setString(3, entradaOriginal.getCardNo());
                    ps.setInt(4, puertaSalidaId);
                    if (entradaOriginal.getUsuarioId() != null) ps.setInt(5, entradaOriginal.getUsuarioId());
                    else ps.setNull(5, Types.INTEGER);
                    if (entradaOriginal.getZonaId() != null) ps.setInt(6, entradaOriginal.getZonaId());
                    else ps.setNull(6, Types.INTEGER);
                    if (entradaOriginal.getZonaId() != null) ps.setInt(7, entradaOriginal.getZonaId());
                    else ps.setNull(7, Types.INTEGER);
                    ps.setString(8, entradaOriginal.getPlaca());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlCerrarEntrada)) {
                    ps.setLong(1, entradaOriginal.getParkingTransNo());
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
