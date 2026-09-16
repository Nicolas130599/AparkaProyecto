package com.aparka.controller;

import com.aparka.model.Zona;
import com.aparka.service.ZonaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Muestra al usuario el mapa/listado de zonas con su nivel de flujo vehicular. */
@WebServlet("/zonas")
public class ZonaServlet extends HttpServlet {

    private final ZonaService zonaService = new ZonaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Zona> zonas = zonaService.obtenerZonasDisponibles();
            req.setAttribute("zonas", zonas);
            req.getRequestDispatcher("/views/zonas.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al listar zonas", e);
        }
    }
}
