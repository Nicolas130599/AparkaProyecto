
package com.aparka.dao;

import com.aparka.model.Entrada;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntradaDAO {

    /**
     * Registra el ingreso de un vehículo respetando la capacidad
     * máxima de la zona.
     *
     * Utiliza una transacción y un bloqueo de fila para coordinar
     * los ingresos concurrentes a una misma zona.
     *
     * @return Número de ticket generado (ParkingTransNo).
     * @throws SQLException Si la zona no existe, está llena
     *                      o se produce un error de base de datos.
     */
    public long registrarIngreso(Entrada e) throws SQLException {

        if (e == null || e.getZonaId() == null || e.getZonaId() <= 0) {
            throw new SQLException("Debe seleccionar una zona válida.");
        }

        String sqlZona =
                "SELECT CapacidadTotal FROM Zona "
                + "WHERE ZonaId = ? FOR UPDATE";

        String sqlOcupacion =
                "SELECT COUNT(*) FROM Entrada "
                + "WHERE ZonaId = ? AND TieneSalida = 0";

        String sqlIngreso =
                "INSERT INTO Entrada "
                + "(CentroComercialId, CardNo, PuertaId, FechaHora, "
                + "UsuarioId, ZonaId, Zona, Placa, TipoClienteId, "
                + "TienePlaca, TieneSalida) "
                + "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, 1, 0)";

        try (Connection con = ConexionBD.obtenerConexion()) {

            con.setAutoCommit(false);

            try {
                int capacidad;

                // Bloquear la fila de la zona.
                try (PreparedStatement ps =
                        con.prepareStatement(sqlZona)) {

                    ps.setInt(1, e.getZonaId());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "La zona seleccionada no existe."
                            );
                        }

                        capacidad = rs.getInt("CapacidadTotal");
                    }
                }

                // Consultar ocupación actual.
                int ocupados;

                try (PreparedStatement ps =
                        con.prepareStatement(sqlOcupacion)) {

                    ps.setInt(1, e.getZonaId());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo consultar la ocupación."
                            );
                        }

                        ocupados = rs.getInt(1);
                    }
                }

                // Impedir que se supere la capacidad.
                if (capacidad <= 0 || ocupados >= capacidad) {
                    throw new SQLException(
                            "La zona está llena. "
                            + "No hay espacios disponibles."
                    );
                }

                long ticket;

                // Registrar el vehículo.
                try (PreparedStatement ps =
                        con.prepareStatement(
                                sqlIngreso,
                                Statement.RETURN_GENERATED_KEYS
                        )) {

                    ps.setInt(1, e.getCentroComercialId());
                    ps.setString(2, e.getCardNo());
                    ps.setInt(3, e.getPuertaId());

                    if (e.getUsuarioId() != null) {
                        ps.setInt(4, e.getUsuarioId());
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }

                    ps.setInt(5, e.getZonaId());
                    ps.setInt(6, e.getZonaId());
                    ps.setString(7, e.getPlaca());

                    if (e.getTipoClienteId() != null) {
                        ps.setInt(8, e.getTipoClienteId());
                    } else {
                        ps.setNull(8, Types.INTEGER);
                    }

                    int filas = ps.executeUpdate();

                    if (filas != 1) {
                        throw new SQLException(
                                "No se pudo registrar el ingreso."
                        );
                    }

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo obtener el ticket generado."
                            );
                        }

                        ticket = rs.getLong(1);
                    }
                }

                // Confirmar el ingreso.
                con.commit();

                return ticket;

            } catch (SQLException | RuntimeException ex) {

                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }

                throw ex;

            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Marca una entrada como finalizada.
     * Registra la salida del vehículo.
     */
    public boolean registrarSalida(long parkingTransNo)
            throws SQLException {

        String sql =
                "UPDATE Entrada SET TieneSalida = 1 "
                + "WHERE ParkingTransNo = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, parkingTransNo);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene el historial de ingresos de un usuario.
     */
    public List<Entrada> listarPorUsuario(int usuarioId)
            throws SQLException {

        List<Entrada> lista = new ArrayList<>();

        String sql =
                "SELECT * FROM Entrada "
                + "WHERE UsuarioId = ? "
                + "ORDER BY FechaHora DESC";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    /**
     * Busca una entrada abierta por número de ticket o placa.
     * Se utiliza para registrar la salida del vehículo.
     */
    public Entrada buscarAbiertaPorTicketOPlaca(String criterio)
            throws SQLException {

        String sql =
                "SELECT * FROM Entrada "
                + "WHERE TieneSalida = 0 "
                + "AND (Placa = ? "
                + "OR CAST(ParkingTransNo AS CHAR) = ?) "
                + "ORDER BY FechaHora DESC LIMIT 1";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, criterio);
            ps.setString(2, criterio);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }

        return null;
    }

    /**
     * Convierte un registro de ResultSet
     * en un objeto Entrada.
     */
    private Entrada mapear(ResultSet rs) throws SQLException {

        Entrada e = new Entrada();

        e.setParkingTransNo(rs.getLong("ParkingTransNo"));
        e.setCentroComercialId(rs.getInt("CentroComercialId"));
        e.setCardNo(rs.getString("CardNo"));
        e.setPuertaId(rs.getInt("PuertaId"));
        e.setFechaHora(rs.getTimestamp("FechaHora"));

        int usuarioId = rs.getInt("UsuarioId");
        e.setUsuarioId(
                rs.wasNull() ? null : usuarioId
        );

        int zonaId = rs.getInt("ZonaId");
        e.setZonaId(
                rs.wasNull() ? null : zonaId
        );

        e.setPlaca(rs.getString("Placa"));
        e.setTieneSalida(rs.getInt("TieneSalida"));

        int tipoClienteId = rs.getInt("TipoClienteId");
        e.setTipoClienteId(
                rs.wasNull() ? null : tipoClienteId
        );

        return e;
    }
}
