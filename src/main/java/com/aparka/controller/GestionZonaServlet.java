package com.aparka.controller;

import com.aparka.model.Zona;
import com.aparka.model.UsuarioSistema;
import com.aparka.service.ZonaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** CRUD de zonas de estacionamiento (solo administrador, protegido por RoleFilter). */
@WebServlet("/admin/zonas")
public class GestionZonaServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(GestionZonaServlet.class);

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
        String admin = nombreAdminEnSesion(req);

        try {
            switch (accion) {
                case "crear":
                    Zona nueva = construirZona(req, false);
                    zonaService.crearZona(nueva);
                    log.info("Admin '{}' creó la zona '{}'", admin, nueva.getNombre());
                    break;
                case "editar":
                    Zona editada = construirZona(req, true);
                    zonaService.actualizarZona(editada);
                    log.info("Admin '{}' editó la zona id={} ('{}')", admin, editada.getZonaId(), editada.getNombre());
                    break;
                case "eliminar":
                    int id = Integer.parseInt(req.getParameter("id"));
                    zonaService.eliminarZona(id);
                    log.warn("Admin '{}' eliminó la zona id={}", admin, id);
                    break;
                default:
                    break;
            }
            resp.sendRedirect(req.getContextPath() + "/admin/zonas");
        } catch (SQLException e) {
            log.error("Error al procesar zona (acción={}) por admin='{}'", accion, admin, e);
            throw new ServletException("Error al procesar la zona", e);
        }
    }

    private String nombreAdminEnSesion(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return "desconocido";
        UsuarioSistema u = (UsuarioSistema) session.getAttribute("usuario");
        return u != null ? u.getUsername() : "desconocido";
    }

    private Zona construirZona(HttpServletRequest req, boolean conId) {
        Zona z = new Zona();
        if (conId) z.setZonaId(Integer.parseInt(req.getParameter("id")));
        z.setCentroComercialId(1); // único centro comercial en esta versión académica
        z.setNombre(req.getParameter("nombre"));
        z.setUbicacion(req.getParameter("ubicacion"));
        z.setCapacidadTotal(Integer.parseInt(req.getParameter("capacidadTotal")));
        z.setTarifaHora(Double.parseDouble(req.getParameter("tarifaHora")));
        return z;
    }
}
