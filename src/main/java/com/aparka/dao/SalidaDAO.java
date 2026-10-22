
package com.aparka.dao;

import com.aparka.model.Entrada;
import com.aparka.util.ConexionBD;

import java.sql.*;

public class SalidaDAO {

    /**
     * Registra la salida de un vehículo.
     * Evita salidas duplicadas mediante bloqueo de fila
     * y una transacción SQL.
     */
    public boolean registrarSalida(
            Entrada entradaOriginal,
            int puertaSalidaId) throws SQLException {

        String sqlBloquear =
                "SELECT TieneSalida FROM Entrada " +
                "WHERE ParkingTransNo = ? FOR UPDATE";

        String sqlInsert =
                "INSERT INTO Salida " +
                "(EntradaParkingTransNo, CentroComercialId, CardNo, " +
                "PuertaId, FechaHora, UsuarioId, ZonaId, Zona, " +
                "Placa, TienePlaca, TieneEntrada) " +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?, 1, 1)";

        String sqlCerrarEntrada =
                "UPDATE Entrada SET TieneSalida = 1 " +
                "WHERE ParkingTransNo = ? AND TieneSalida = 0";

        try (Connection con = ConexionBD.obtenerConexion()) {

            con.setAutoCommit(false);

            try {

                // 1. Bloquear el registro de entrada.
                try (PreparedStatement ps =
                        con.prepareStatement(sqlBloquear)) {

                    ps.setLong(1, entradaOriginal.getParkingTransNo());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()
                                || rs.getInt("TieneSalida") != 0) {

                            con.rollback();
                            return false;
                        }
                    }
                }

                // 2. Insertar el registro de salida.
                try (PreparedStatement ps =
                        con.prepareStatement(sqlInsert)) {

                    ps.setLong(1, entradaOriginal.getParkingTransNo());
                    ps.setInt(2, entradaOriginal.getCentroComercialId());
                    ps.setString(3, entradaOriginal.getCardNo());
                    ps.setInt(4, puertaSalidaId);

                    if (entradaOriginal.getUsuarioId() != null) {
                        ps.setInt(5, entradaOriginal.getUsuarioId());
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }

                    if (entradaOriginal.getZonaId() != null) {
                        ps.setInt(6, entradaOriginal.getZonaId());
                        ps.setInt(7, entradaOriginal.getZonaId());
                    } else {
                        ps.setNull(6, Types.INTEGER);
                        ps.setNull(7, Types.INTEGER);
                    }

                    ps.setString(8, entradaOriginal.getPlaca());

                    ps.executeUpdate();
                }

                // 3. Cerrar la entrada únicamente si sigue activa.
                try (PreparedStatement ps =
                        con.prepareStatement(sqlCerrarEntrada)) {

                    ps.setLong(1, entradaOriginal.getParkingTransNo());

                    int filasActualizadas = ps.executeUpdate();

                    if (filasActualizadas != 1) {
                        con.rollback();
                        return false;
                    }
                }

                // 4. Confirmar la transacción.
                con.commit();

                return true;

            } catch (SQLException e) {

                con.rollback();
                throw e;
            }
        }
    }
}
