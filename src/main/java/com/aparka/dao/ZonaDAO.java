package com.aparka.dao;

import com.aparka.model.Zona;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ZonaDAO {

    public List<Zona> listarTodas() throws SQLException {
        List<Zona> zonas = new ArrayList<>();
        String sql = "SELECT * FROM zona ORDER BY nombre";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) zonas.add(mapear(rs));
        }
        return zonas;
    }

    /** Zonas ordenadas de mayor a menor flujo vehicular (para el reporte del admin). */
    public List<Zona> listarOrdenadasPorFlujo() throws SQLException {
        List<Zona> zonas = listarTodas();
        zonas.sort((a, b) -> Double.compare(b.getPorcentajeOcupacion(), a.getPorcentajeOcupacion()));
        return zonas;
    }

    public boolean insertar(Zona z) throws SQLException {
        String sql = "INSERT INTO zona (nombre, ubicacion, capacidad_total, capacidad_ocupada, tarifa_hora) VALUES (?,?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, z.getNombre());
            ps.setString(2, z.getUbicacion());
            ps.setInt(3, z.getCapacidadTotal());
            ps.setInt(4, z.getCapacidadOcupada());
            ps.setDouble(5, z.getTarifaHora());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Zona z) throws SQLException {
        String sql = "UPDATE zona SET nombre=?, ubicacion=?, capacidad_total=?, capacidad_ocupada=?, tarifa_hora=? WHERE id=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, z.getNombre());
            ps.setString(2, z.getUbicacion());
            ps.setInt(3, z.getCapacidadTotal());
            ps.setInt(4, z.getCapacidadOcupada());
            ps.setDouble(5, z.getTarifaHora());
            ps.setInt(6, z.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM zona WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Suma o resta 1 a la ocupación de la zona (ingreso/salida de vehículo). */
    public void ajustarOcupacion(int zonaId, int delta) throws SQLException {
        String sql = "UPDATE zona SET capacidad_ocupada = capacidad_ocupada + ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, zonaId);
            ps.executeUpdate();
        }
    }

    private Zona mapear(ResultSet rs) throws SQLException {
        return new Zona(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("ubicacion"),
                rs.getInt("capacidad_total"),
                rs.getInt("capacidad_ocupada"),
                rs.getDouble("tarifa_hora")
        );
    }
}
