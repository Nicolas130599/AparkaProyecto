package com.aparka.dao;

import com.aparka.model.Movimiento;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Ingresos agrupados por zona y por hora del día, para el mapa de calor.
     * Incluye TODAS las zonas (aunque no tengan ingresos en el rango).
     *
     * @param fechaInicio formato "yyyy-MM-dd"
     * @param fechaFin    formato "yyyy-MM-dd"
     * @return nombre de zona -> 24 contadores (índice = hora, 0 = 00:00)
     */
    public Map<String, int[]> flujoPorZonaYHora(String fechaInicio, String fechaFin) throws SQLException {
        Map<String, int[]> resultado = new LinkedHashMap<>();
        String sql = "SELECT z.Nombre AS ZonaNombre, HOUR(e.FechaHora) AS Hora, COUNT(e.ParkingTransNo) AS Total " +
                "FROM Zona z " +
                "LEFT JOIN Entrada e ON e.ZonaId = z.ZonaId AND DATE(e.FechaHora) BETWEEN ? AND ? " +
                "GROUP BY z.ZonaId, z.Nombre, HOUR(e.FechaHora) " +
                "ORDER BY z.Nombre, Hora";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int[] horas = resultado.computeIfAbsent(rs.getString("ZonaNombre"), k -> new int[24]);
                    int hora = rs.getInt("Hora");
                    if (!rs.wasNull() && hora >= 0 && hora < 24) { // Hora es NULL en zonas sin ingresos
                        horas[hora] += rs.getInt("Total");
                    }
                }
            }
        }
        return resultado;
    }
}
