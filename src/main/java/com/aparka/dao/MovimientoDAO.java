package com.aparka.dao;

import com.aparka.model.Movimiento;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    /**
     * Consulta combinada de Entrada + Salida, con filtros opcionales de fecha, zona y tipo.
     * @param fechaInicio formato "yyyy-MM-dd" (obligatorio)
     * @param fechaFin    formato "yyyy-MM-dd" (obligatorio)
     * @param zonaId      null = todas las zonas
     * @param tipo        "ENTRADA", "SALIDA" o null (todos)
     */
    public List<Movimiento> buscarConFiltros(String fechaInicio, String fechaFin, Integer zonaId, String tipo)
            throws SQLException {

        List<Movimiento> resultado = new ArrayList<>();
        boolean incluirEntradas = tipo == null || "ENTRADA".equalsIgnoreCase(tipo);
        boolean incluirSalidas = tipo == null || "SALIDA".equalsIgnoreCase(tipo);

        StringBuilder sql = new StringBuilder();
        if (incluirEntradas) {
            sql.append("SELECT 'Entrada' AS Tipo, e.ParkingTransNo AS Ticket, e.Placa, e.FechaHora, ")
               .append("p.Descripcion AS Puerta, z.Nombre AS ZonaNombre ")
               .append("FROM Entrada e ")
               .append("LEFT JOIN PuertaMaster p ON e.PuertaId = p.PuertaId ")
               .append("LEFT JOIN Zona z ON e.ZonaId = z.ZonaId ")
               .append("WHERE DATE(e.FechaHora) BETWEEN ? AND ? ")
               .append(zonaId != null ? "AND e.ZonaId = ? " : "");
        }
        if (incluirSalidas) {
            if (incluirEntradas) sql.append("UNION ALL ");
            sql.append("SELECT 'Salida' AS Tipo, s.ParkingTransNo AS Ticket, s.Placa, s.FechaHora, ")
               .append("p.Descripcion AS Puerta, z.Nombre AS ZonaNombre ")
               .append("FROM Salida s ")
               .append("LEFT JOIN PuertaMaster p ON s.PuertaId = p.PuertaId ")
               .append("LEFT JOIN Zona z ON s.ZonaId = z.ZonaId ")
               .append("WHERE DATE(s.FechaHora) BETWEEN ? AND ? ")
               .append(zonaId != null ? "AND s.ZonaId = ? " : "");
        }
        sql.append("ORDER BY FechaHora DESC");

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int i = 1;
            if (incluirEntradas) {
                ps.setString(i++, fechaInicio);
                ps.setString(i++, fechaFin);
                if (zonaId != null) ps.setInt(i++, zonaId);
            }
            if (incluirSalidas) {
                ps.setString(i++, fechaInicio);
                ps.setString(i++, fechaFin);
                if (zonaId != null) ps.setInt(i++, zonaId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movimiento m = new Movimiento();
                    m.setTipo(rs.getString("Tipo"));
                    m.setTicket(rs.getLong("Ticket"));
                    m.setPlaca(rs.getString("Placa"));
                    m.setFechaHora(rs.getTimestamp("FechaHora"));
                    m.setPuerta(rs.getString("Puerta"));
                    m.setZona(rs.getString("ZonaNombre"));
                    resultado.add(m);
                }
            }
        }
        return resultado;
    }
}
