package com.aparka.controller;

import com.aparka.model.Zona;
import com.aparka.service.ZonaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** CRUD de zonas de estacionamiento (solo administrador, protegido por RoleFilter). */
@WebServlet("/admin/zonas")
public class GestionZonaServlet extends HttpServlet {

    private final ZonaService zonaService = new ZonaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Zona> zonas = zonaService.obtenerZonasDisponibles();
            req.setAttribute("zonas", zonas);
            req.getRequestDispatcher("/views/gestion_zonas.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al listar zonas", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");

        try {
            switch (accion) {
                case "crear":
                    zonaService.crearZona(construirZona(req, false));
                    break;
                case "editar":
                    zonaService.actualizarZona(construirZona(req, true));
                    break;
                case "eliminar":
                    zonaService.eliminarZona(Integer.parseInt(req.getParameter("id")));
                    break;
                default:
                    break;
            }
            resp.sendRedirect(req.getContextPath() + "/admin/zonas");
        } catch (SQLException e) {
            throw new ServletException("Error al procesar la zona", e);
        }
    }

    private Zona construirZona(HttpServletRequest req, boolean conId) {
        Zona z = new Zona();
        if (conId) z.setId(Integer.parseInt(req.getParameter("id")));
        z.setNombre(req.getParameter("nombre"));
        z.setUbicacion(req.getParameter("ubicacion"));
        z.setCapacidadTotal(Integer.parseInt(req.getParameter("capacidadTotal")));
        z.setCapacidadOcupada(Integer.parseInt(req.getParameter("capacidadOcupada")));
        z.setTarifaHora(Double.parseDouble(req.getParameter("tarifaHora")));
        return z;
    }
}
