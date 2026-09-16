package com.aparka.controller;

import com.aparka.model.Zona;
import com.aparka.service.ZonaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Panel del administrador: ranking de zonas por flujo vehicular. */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final ZonaService zonaService = new ZonaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Zona> ranking = zonaService.obtenerRankingFlujoVehicular();
            req.setAttribute("ranking", ranking);
            req.getRequestDispatcher("/views/admin_dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al cargar el dashboard", e);
        }
    }
}
