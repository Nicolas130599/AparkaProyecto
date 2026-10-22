
package com.aparka.controller;

import com.aparka.model.Zona;
import com.aparka.service.ZonaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final ZonaService zonaService = new ZonaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            List<Zona> ranking =
                    zonaService.obtenerRankingFlujoVehicular();

            int totalZonas = ranking.size();
            int capacidadTotal = 0;
            int espaciosOcupados = 0;

            for (Zona zona : ranking) {
                capacidadTotal += zona.getCapacidadTotal();
                espaciosOcupados += zona.getCapacidadOcupada();
            }

            int espaciosDisponibles =
                    Math.max(0, capacidadTotal - espaciosOcupados);

            double porcentajeOcupacion = capacidadTotal > 0
                    ? (espaciosOcupados * 100.0) / capacidadTotal
                    : 0.0;

            req.setAttribute("ranking", ranking);
            req.setAttribute("totalZonas", totalZonas);
            req.setAttribute("capacidadTotal", capacidadTotal);
            req.setAttribute("espaciosOcupados", espaciosOcupados);
            req.setAttribute("espaciosDisponibles", espaciosDisponibles);
            req.setAttribute("porcentajeOcupacion", porcentajeOcupacion);

            req.getRequestDispatcher("/views/admin_dashboard.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al cargar el dashboard administrativo", e
            );
        }
    }
}
