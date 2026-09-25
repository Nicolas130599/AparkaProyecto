package com.aparka.dao;

import com.aparka.model.Zona;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ZonaDAO {

    /**
     * Trae todas las zonas y calcula la ocupación real contando cuántas filas
     * de Entrada tienen TieneSalida = 0 (vehículos que aún no han salido) para esa zona.
     */
    public List<Zona> listarTodas() throws SQLException {
        List<Zona> zonas = new ArrayList<>();
        String sql = "SELECT z.*, " +
                "(SELECT COUNT(*) FROM Entrada e WHERE e.ZonaId = z.ZonaId AND e.TieneSalida = 0) AS ocupados " +
                "FROM Zona z ORDER BY z.Nombre";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) zonas.add(mapear(rs));
        }
        return zonas;
    }

    public List<Zona> listarOrdenadasPorFlujo() throws SQLException {
        List<Zona> zonas = listarTodas();
        zonas.sort((a, b) -> Double.compare(b.getPorcentajeOcupacion(), a.getPorcentajeOcupacion()));
        return zonas;
    }

    public Zona obtenerPorId(int zonaId) throws SQLException {
        String sql = "SELECT z.*, " +
                "(SELECT COUNT(*) FROM Entrada e WHERE e.ZonaId = z.ZonaId AND e.TieneSalida = 0) AS ocupados " +
                "FROM Zona z WHERE z.ZonaId = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, zonaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public boolean insertar(Zona z) throws SQLException {
        String sql = "INSERT INTO Zona (CentroComercialId, Nombre, Ubicacion, CapacidadTotal, TarifaHora) VALUES (?,?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, z.getCentroComercialId());
            ps.setString(2, z.getNombre());
            ps.setString(3, z.getUbicacion());
            ps.setInt(4, z.getCapacidadTotal());
            ps.setDouble(5, z.getTarifaHora());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Zona z) throws SQLException {
        String sql = "UPDATE Zona SET Nombre=?, Ubicacion=?, CapacidadTotal=?, TarifaHora=? WHERE ZonaId=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, z.getNombre());
            ps.setString(2, z.getUbicacion());
            ps.setInt(3, z.getCapacidadTotal());
            ps.setDouble(4, z.getTarifaHora());
            ps.setInt(5, z.getZonaId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int zonaId) throws SQLException {
        String sql = "DELETE FROM Zona WHERE ZonaId = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, zonaId);
            return ps.executeUpdate() > 0;
        }
    }

    private Zona mapear(ResultSet rs) throws SQLException {
        Zona z = new Zona();
        z.setZonaId(rs.getInt("ZonaId"));
        z.setCentroComercialId(rs.getInt("CentroComercialId"));
        z.setNombre(rs.getString("Nombre"));
        z.setUbicacion(rs.getString("Ubicacion"));
        z.setCapacidadTotal(rs.getInt("CapacidadTotal"));
        z.setCapacidadOcupada(rs.getInt("ocupados"));
        z.setTarifaHora(rs.getDouble("TarifaHora"));
        return z;
    }
}
