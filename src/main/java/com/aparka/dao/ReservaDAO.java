package com.aparka.dao;

import com.aparka.model.Reserva;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    public boolean registrarIngreso(Reserva r) throws SQLException {
        String sql = "INSERT INTO reserva (usuario_id, zona_id, placa, fecha_ingreso) VALUES (?,?,?,NOW())";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getUsuarioId());
            ps.setInt(2, r.getZonaId());
            ps.setString(3, r.getPlaca());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean registrarSalida(int reservaId) throws SQLException {
        String sql = "UPDATE reserva SET fecha_salida = NOW() WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservaId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Reserva> listarPorUsuario(int usuarioId) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reserva WHERE usuario_id = ? ORDER BY fecha_ingreso DESC";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setId(rs.getInt("id"));
        r.setUsuarioId(rs.getInt("usuario_id"));
        r.setZonaId(rs.getInt("zona_id"));
        r.setPlaca(rs.getString("placa"));
        r.setFechaIngreso(rs.getTimestamp("fecha_ingreso"));
        r.setFechaSalida(rs.getTimestamp("fecha_salida"));
        return r;
    }
}
