package com.aparka.dao;

import com.aparka.model.Caja;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a CajaMaster (cajeros / módulos de pago) y a sus pagos registrados en Venta. */
public class CajaDAO {

    /**
     * Lista todos los cajeros con la cantidad de pagos y el monto del rango de fechas (ventas no anuladas).
     * Incluye cajeros sin pagos. Fechas en formato "yyyy-MM-dd".
     */
    public List<Caja> listarConResumen(String fechaInicio, String fechaFin) throws SQLException {
        List<Caja> cajas = new ArrayList<>();
        String sql = "SELECT c.CajaId, c.TipoCaja, c.Ubicacion, c.Nivel, c.PlanoX, c.PlanoY, " +
                "COUNT(v.TransaccionNo) AS pagos, COALESCE(SUM(v.MontoNeto), 0) AS monto " +
                "FROM CajaMaster c " +
                "LEFT JOIN Venta v ON v.CajaId = c.CajaId " +
                "AND DATE(v.FechaHora) BETWEEN ? AND ? AND v.FLAG_DOCUMENTO_ANULADO = 0 " +
                "GROUP BY c.CajaId, c.TipoCaja, c.Ubicacion, c.Nivel, c.PlanoX, c.PlanoY " +
                "ORDER BY c.CajaId";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Caja c = new Caja();
                    c.setCajaId(rs.getString("CajaId"));
                    c.setTipoCaja(rs.getString("TipoCaja"));
                    c.setUbicacion(rs.getString("Ubicacion"));
                    c.setNivel(rs.getString("Nivel"));
                    double x = rs.getDouble("PlanoX");
                    c.setPlanoX(rs.wasNull() ? null : x);
                    double y = rs.getDouble("PlanoY");
                    c.setPlanoY(rs.wasNull() ? null : y);
                    c.setPagos(rs.getInt("pagos"));
                    c.setMonto(rs.getDouble("monto"));
                    cajas.add(c);
                }
            }
        }
        return cajas;
    }

    public boolean existe(String cajaId) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM CajaMaster WHERE CajaId = ?")) {
            ps.setString(1, cajaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean insertar(Caja c) throws SQLException {
        String sql = "INSERT INTO CajaMaster (CajaId, CentroComercialId, TipoCaja, Ubicacion, Nivel, PlanoX, PlanoY) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCajaId());
            ps.setInt(2, 1); // único centro comercial en esta versión académica
            ps.setString(3, c.getTipoCaja());
            ps.setString(4, c.getUbicacion());
            ps.setString(5, c.getNivel());
            ps.setObject(6, c.getPlanoX());
            ps.setObject(7, c.getPlanoY());
            return ps.executeUpdate() > 0;
        }
    }

    /** El identificador (CajaId) no se modifica: lo referencian las ventas. */
    public boolean actualizar(Caja c) throws SQLException {
        String sql = "UPDATE CajaMaster SET TipoCaja=?, Ubicacion=?, Nivel=?, PlanoX=?, PlanoY=? WHERE CajaId=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getTipoCaja());
            ps.setString(2, c.getUbicacion());
            ps.setString(3, c.getNivel());
            ps.setObject(4, c.getPlanoX());
            ps.setObject(5, c.getPlanoY());
            ps.setString(6, c.getCajaId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Falla (SQLState 23xxx) si el cajero tiene ventas registradas. */
    public boolean eliminar(String cajaId) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM CajaMaster WHERE CajaId = ?")) {
            ps.setString(1, cajaId);
            return ps.executeUpdate() > 0;
        }
    }
}
