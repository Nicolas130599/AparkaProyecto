package com.aparka.dao;

import com.aparka.model.Entrada;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntradaDAO {

    /** Registra el ingreso de un vehículo a una zona. Devuelve el N° de Ticket generado (ParkingTransNo). */
    public long registrarIngreso(Entrada e) throws SQLException {
        String sql = "INSERT INTO Entrada (CentroComercialId, CardNo, PuertaId, FechaHora, UsuarioId, ZonaId, Zona, Placa, TipoClienteId, TienePlaca, TieneSalida) " +
                "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, 1, 0)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getCentroComercialId());
            ps.setString(2, e.getCardNo());
            ps.setInt(3, e.getPuertaId());
            if (e.getUsuarioId() != null) ps.setInt(4, e.getUsuarioId()); else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, e.getZonaId());
            ps.setInt(6, e.getZonaId());
            ps.setString(7, e.getPlaca());
            if (e.getTipoClienteId() != null) ps.setInt(8, e.getTipoClienteId()); else ps.setNull(8, Types.INTEGER);

            ps.executeUpdate();

            try (ResultSet generadas = ps.getGeneratedKeys()) {
                if (generadas.next()) return generadas.getLong(1);
            }
            return 0;
        }
    }

    /** Marca una entrada como finalizada (registra la salida del vehículo). */
    public boolean registrarSalida(long parkingTransNo) throws SQLException {
        String sql = "UPDATE Entrada SET TieneSalida = 1 WHERE ParkingTransNo = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, parkingTransNo);
            return ps.executeUpdate() > 0;
        }
    }

    /** Historial de ingresos de un usuario logueado (para su perfil). */
    public List<Entrada> listarPorUsuario(int usuarioId) throws SQLException {
        List<Entrada> lista = new ArrayList<>();
        String sql = "SELECT * FROM Entrada WHERE UsuarioId = ? ORDER BY FechaHora DESC";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Busca una entrada SIN salida (TieneSalida=0) por número de ticket o por placa. Usado en 'Registrar Salida'. */
    public Entrada buscarAbiertaPorTicketOPlaca(String criterio) throws SQLException {
        String sql = "SELECT * FROM Entrada WHERE TieneSalida = 0 " +
                "AND (Placa = ? OR CAST(ParkingTransNo AS CHAR) = ?) " +
                "ORDER BY FechaHora DESC LIMIT 1";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, criterio);
            ps.setString(2, criterio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    private Entrada mapear(ResultSet rs) throws SQLException {
        Entrada e = new Entrada();
        e.setParkingTransNo(rs.getLong("ParkingTransNo"));
        e.setCentroComercialId(rs.getInt("CentroComercialId"));
        e.setCardNo(rs.getString("CardNo"));
        e.setPuertaId(rs.getInt("PuertaId"));
        e.setFechaHora(rs.getTimestamp("FechaHora"));
        int usuarioId = rs.getInt("UsuarioId");
        e.setUsuarioId(rs.wasNull() ? null : usuarioId);
        int zonaId = rs.getInt("ZonaId");
        e.setZonaId(rs.wasNull() ? null : zonaId);
        e.setPlaca(rs.getString("Placa"));
        e.setTieneSalida(rs.getInt("TieneSalida"));
        int tipoClienteId = rs.getInt("TipoClienteId");
        e.setTipoClienteId(rs.wasNull() ? null : tipoClienteId);
        return e;
    }
}
