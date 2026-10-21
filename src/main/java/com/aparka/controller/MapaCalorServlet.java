package com.aparka.controller;

import com.aparka.dao.MovimientoDAO;
import com.aparka.model.MapaCalor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Mapa de calor del flujo vehicular: ingresos por zona y hora del día en un rango de fechas.
 * Está bajo /admin/*, así que el RoleFilter ya restringe el acceso a administradores.
 * Por defecto muestra los últimos 7 días.
 */
@WebServlet("/admin/mapa-calor")
public class MapaCalorServlet extends HttpServlet {

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LocalDate hoy = LocalDate.now();
        LocalDate fin = parsearFecha(req.getParameter("fechaFin"), hoy);
        LocalDate inicio = parsearFecha(req.getParameter("fechaInicio"), hoy.minusDays(7));
        if (inicio.isAfter(fin)) { // si las puso al revés, las intercambia
            LocalDate tmp = inicio;
            inicio = fin;
            fin = tmp;
        }

        try {
            MapaCalor mapa = MapaCalor.desde(movimientoDAO.flujoPorZonaYHora(inicio.toString(), fin.toString()));
            req.setAttribute("mapa", mapa);
            req.setAttribute("fechaInicio", inicio.toString());
            req.setAttribute("fechaFin", fin.toString());
            req.getRequestDispatcher("/views/mapa_calor.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al generar el mapa de calor", e);
        }
    }

    private LocalDate parsearFecha(String valor, LocalDate porDefecto) {
        if (valor == null || valor.isBlank()) return porDefecto;
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            return porDefecto;
        }
    }
}
