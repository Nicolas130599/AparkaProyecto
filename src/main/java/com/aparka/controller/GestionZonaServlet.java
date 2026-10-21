package com.aparka.controller;

import com.aparka.dao.CajaDAO;

import com.aparka.dao.MovimientoDAO;

import com.aparka.model.Caja;

import com.aparka.model.MapaCalor;

import com.aparka.model.ResumenNivel;

import com.aparka.model.UsuarioSistema;

import com.aparka.model.Zona;

import com.aparka.service.ZonaService;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.*;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.sql.SQLException;

import java.time.LocalDate;

import java.time.format.DateTimeParseException;

import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

/**
 * Gestión de zonas y cajeros: CRUD, flujo, pagos y planos por nivel.
 * Acceso protegido mediante RoleFilter.
 */
@WebServlet("/admin/zonas")

public class GestionZonaServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(GestionZonaServlet.class);
    /** Planos disponibles en /img/planos: pisos P1-P7 y sótanos S1-S3. */
    static final List<String> NIVELES = List.of("P1", "P2", "P3", "P4", "P5", "P6", "P7", "S1", "S2", "S3");

    private final ZonaService zonaService = new ZonaService();

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    private final CajaDAO cajaDAO = new CajaDAO();

    @Override

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)

            throws ServletException, IOException {

        LocalDate hoy = LocalDate.now();

        LocalDate fin = parsearFecha(req.getParameter("fechaFin"), hoy);

        LocalDate inicio = parsearFecha(req.getParameter("fechaInicio"), hoy.minusDays(7));

        if (inicio.isAfter(fin)) {

            LocalDate tmp = inicio;

            inicio = fin;

            fin = tmp;

        }

        try {

            List<Zona> zonas = zonaService.obtenerZonasDisponibles();

            MapaCalor mapa = MapaCalor.desde(movimientoDAO.flujoPorZonaYHora(inicio.toString(), fin.toString()));

            //* Cajeros con sus pagos del rango. Si aún no se ejecutó la migración de cajeros, la pantalla sigue funcionando.*

            String capa = "cajeros".equals(req.getParameter("capa")) ? "cajeros" : "zonas";

            List<Caja> cajas = new ArrayList<>();

            boolean cajasDisponibles = true;

            try {

                cajas = cajaDAO.listarConResumen(inicio.toString(), fin.toString());

                Caja.calcularIntensidad(cajas);

            } catch (SQLException e) {

                log.warn("No se pudieron cargar los cajeros (¿columnas de CajaMaster aún no creadas?): {}", e.getMessage());

                cajasDisponibles = false;

            }

            //* Elementos ubicados por nivel en la capa activa (para los contadores de las pestañas)*

            Map<String, Integer> elementosPorNivel = new LinkedHashMap<>();

            if ("zonas".equals(capa)) {

                for (Zona z : zonas) {

                    if (z.isTienePosicion() && nivelValido(z.getNivel())) elementosPorNivel.merge(z.getNivel(), 1, Integer::sum);

                }

            } else {

                for (Caja c : cajas) {

                    if (c.isTienePosicion() && nivelValido(c.getNivel())) elementosPorNivel.merge(c.getNivel(), 1, Integer::sum);

                }

            }

            //* Nivel (plano) a mostrar: el pedido si es válido; si no, el primero que tenga elementos ubicados*

            String nivel = req.getParameter("nivel");

            if (!nivelValido(nivel)) {

                nivel = NIVELES.stream().filter(elementosPorNivel::containsKey).findFirst().orElse("P1");

            }

            List<Zona> zonasDelNivel = new ArrayList<>();

            for (Zona z : zonas) {

                if (z.isTienePosicion() && nivel.equals(z.getNivel())) zonasDelNivel.add(z);

            }

            List<Caja> cajasDelNivel = new ArrayList<>();

            for (Caja c : cajas) {

                if (c.isTienePosicion() && nivel.equals(c.getNivel())) cajasDelNivel.add(c);

            }

            int totalPagos = 0;

            for (Caja c : cajas) totalPagos += c.getPagos();

            int capacidad = 0, ocupados = 0;

            for (Zona z : zonas) {

                capacidad += z.getCapacidadTotal();

                ocupados += z.getCapacidadOcupada();

            }

            req.setAttribute("zonas", zonas);

            req.setAttribute("mapa", mapa);

            req.setAttribute("niveles", NIVELES);

            req.setAttribute("nivelActual", nivel);

            req.setAttribute("capa", capa);

            req.setAttribute("elementosPorNivel", elementosPorNivel);

            req.setAttribute("zonasDelNivel", zonasDelNivel);

            req.setAttribute("cajas", cajas);

            req.setAttribute("cajasDelNivel", cajasDelNivel);

            req.setAttribute("cajasDisponibles", cajasDisponibles);

            req.setAttribute("totalPagos", totalPagos);

            req.setAttribute("resumenNiveles", ResumenNivel.construir(NIVELES, zonas, mapa, cajas));

            req.setAttribute("totalCapacidad", capacidad);

            req.setAttribute("totalOcupados", ocupados);

            req.setAttribute("porcentajeGlobal", capacidad == 0 ? 0 : Math.round(ocupados * 100.0 / capacidad));

            req.setAttribute("fechaInicio", inicio.toString());

            req.setAttribute("fechaFin", fin.toString());

            req.getRequestDispatcher("/views/gestion_zonas.jsp").forward(req, resp);

        } catch (SQLException e) {

            throw new ServletException("Error al listar zonas", e);

        }

    }

    @Override

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)

            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String accion = req.getParameter("accion");

        String admin = nombreAdminEnSesion(req);

        String destino = req.getContextPath() + "/admin/zonas";

        try {

            if ("crear".equals(accion)) {

                Zona nueva = construirZona(req, false);

                zonaService.crearZona(nueva);

                log.info("Admin '{}' creó la zona '{}'", admin, nueva.getNombre());

            } else if ("editar".equals(accion)) {

                Zona editada = construirZona(req, true);

                zonaService.actualizarZona(editada);

                log.info("Admin '{}' editó la zona id={} ('{}')", admin, editada.getZonaId(), editada.getNombre());

            } else if ("eliminar".equals(accion)) {

                int id = Integer.parseInt(req.getParameter("id"));

                zonaService.eliminarZona(id);

                log.warn("Admin '{}' eliminó la zona id={}", admin, id);

            }

            resp.sendRedirect(destino + "?ok=1");

        } catch (IllegalArgumentException e) { //* incluye NumberFormatException*

            log.warn("Datos de zona inválidos (acción={}) enviados por admin='{}': {}", accion, admin, e.getMessage());

            resp.sendRedirect(destino + "?error=1");

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
    /** Construye la zona y valida los datos del formulario. */
    private Zona construirZona(HttpServletRequest req, boolean conId) {

        Zona z = new Zona();

        if (conId) z.setZonaId(Integer.parseInt(req.getParameter("id")));

        z.setCentroComercialId(1); //* único centro comercial en esta versión académica*

        String nombre = req.getParameter("nombre") == null ? "" : req.getParameter("nombre").trim();

        if (nombre.isEmpty() || nombre.length() > 100) throw new IllegalArgumentException("nombre");

        z.setNombre(nombre);

        String ubicacion = req.getParameter("ubicacion") == null ? "" : req.getParameter("ubicacion").trim();

        if (ubicacion.length() > 150) throw new IllegalArgumentException("ubicacion");

        z.setUbicacion(ubicacion);

        int capacidad = Integer.parseInt(req.getParameter("capacidadTotal").trim());

        double tarifa = Double.parseDouble(req.getParameter("tarifaHora").trim());

        if (capacidad < 0 || tarifa < 0) throw new IllegalArgumentException("valores negativos");

        z.setCapacidadTotal(capacidad);

        z.setTarifaHora(tarifa);

        //* Ubicación en el plano (opcional): nivel válido + posición en %; si falta algo, queda sin ubicar*

        String nivel = req.getParameter("nivel");

        Double x = porcentaje(req.getParameter("planoX"));

        Double y = porcentaje(req.getParameter("planoY"));

        if (nivelValido(nivel) && x != null && y != null) {

            z.setNivel(nivel);

            z.setPlanoX(x);

            z.setPlanoY(y);

        } else if (nivelValido(nivel)) {

            z.setNivel(nivel); //* nivel asignado, aún sin posición*

        }

        return z;

    }

    private static boolean nivelValido(String nivel) {
        return nivel != null && NIVELES.contains(nivel);
    }

    static Double porcentaje(String valor) {

        if (valor == null || valor.isBlank()) return null;

        double v = Double.parseDouble(valor.trim());

        return Math.max(0, Math.min(100, Math.round(v * 100) / 100.0));

    }

    private static LocalDate parsearFecha(String valor, LocalDate porDefecto) {

        if (valor == null || valor.isBlank()) return porDefecto;

        try {

            return LocalDate.parse(valor.trim());

        } catch (DateTimeParseException e) {

            return porDefecto;

        }

    }

}
