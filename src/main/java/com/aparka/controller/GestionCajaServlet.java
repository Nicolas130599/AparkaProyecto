package com.aparka.controller;

import com.aparka.dao.CajaDAO;
import com.aparka.model.Caja;
import com.aparka.model.UsuarioSistema;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/** Alta, edición y baja de cajeros / módulos de pago (solo administrador, protegido por RoleFilter en /admin/*). */
@WebServlet("/admin/cajas")
public class GestionCajaServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(GestionCajaServlet.class);
    private final CajaDAO cajaDAO = new CajaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/zonas?capa=cajeros");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");
        String admin = nombreAdminEnSesion(req);
        String destino = req.getContextPath() + "/admin/zonas?capa=cajeros";

        try {
            if ("crear".equals(accion)) {
                Caja nueva = construirCaja(req);
                if (cajaDAO.existe(nueva.getCajaId())) {
                    resp.sendRedirect(destino + "&error=2");
                    return;
                }
                cajaDAO.insertar(nueva);
                log.info("Admin '{}' creó el cajero '{}'", admin, nueva.getCajaId());
            } else if ("editar".equals(accion)) {
                Caja editada = construirCaja(req);
                cajaDAO.actualizar(editada);
                log.info("Admin '{}' editó el cajero '{}'", admin, editada.getCajaId());
            } else if ("eliminar".equals(accion)) {
                String id = limpiar(req.getParameter("id"));
                cajaDAO.eliminar(id);
                log.warn("Admin '{}' eliminó el cajero '{}'", admin, id);
            }
            resp.sendRedirect(destino + "&ok=1");
        } catch (IllegalArgumentException e) {
            log.warn("Datos de cajero inválidos (acción={}) enviados por admin='{}': {}", accion, admin, e.getMessage());
            resp.sendRedirect(destino + "&error=1");
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { // restricción de integridad (tiene ventas)
                log.warn("No se pudo eliminar/guardar el cajero por restricción de integridad: {}", e.getMessage());
                resp.sendRedirect(destino + "&error=3");
                return;
            }
            log.error("Error al procesar cajero (acción={}) por admin='{}'", accion, admin, e);
            throw new ServletException("Error al procesar el cajero", e);
        }
    }

    private Caja construirCaja(HttpServletRequest req) {
        Caja c = new Caja();
        String id = limpiar(req.getParameter("cajaId"));
        if (!id.matches("[A-Za-z0-9_-]{1,30}")) throw new IllegalArgumentException("cajaId");
        c.setCajaId(id);

        String tipo = limpiar(req.getParameter("tipoCaja"));
        String ubicacion = limpiar(req.getParameter("ubicacion"));
        if (tipo.length() > 50 || ubicacion.length() > 100) throw new IllegalArgumentException("longitud");
        c.setTipoCaja(tipo);
        c.setUbicacion(ubicacion);

        String nivel = req.getParameter("nivel");
        Double x = GestionZonaServlet.porcentaje(req.getParameter("planoX"));
        Double y = GestionZonaServlet.porcentaje(req.getParameter("planoY"));
        if (GestionZonaServlet.NIVELES.contains(nivel)) {
            c.setNivel(nivel);
            if (x != null && y != null) {
                c.setPlanoX(x);
                c.setPlanoY(y);
            }
        }
        return c;
    }

    private String nombreAdminEnSesion(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        UsuarioSistema u = session == null ? null : (UsuarioSistema) session.getAttribute("usuario");
        return u != null ? u.getUsername() : "desconocido";
    }

    private static String limpiar(String v) { return v == null ? "" : v.trim(); }
}
