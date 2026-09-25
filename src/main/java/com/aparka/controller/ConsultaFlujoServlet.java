package com.aparka.controller;

import com.aparka.dao.MovimientoDAO;
import com.aparka.model.Movimiento;
import com.aparka.model.Zona;
import com.aparka.service.ZonaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Consulta de flujo vehicular: tabla de ingresos/salidas filtrable por fecha, zona y tipo. */
@WebServlet("/admin/flujo")
public class ConsultaFlujoServlet extends HttpServlet {

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final ZonaService zonaService = new ZonaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String fechaInicio = obtenerOPorDefecto(req.getParameter("fechaInicio"), LocalDate.now().toString());
            String fechaFin = obtenerOPorDefecto(req.getParameter("fechaFin"), LocalDate.now().toString());
            String zonaIdParam = req.getParameter("zonaId");
            String tipo = req.getParameter("tipo"); // "ENTRADA", "SALIDA" o null/"" = todos

            Integer zonaId = (zonaIdParam != null && !zonaIdParam.isEmpty()) ? Integer.parseInt(zonaIdParam) : null;
            if (tipo != null && tipo.isEmpty()) tipo = null;

            List<Movimiento> movimientos = movimientoDAO.buscarConFiltros(fechaInicio, fechaFin, zonaId, tipo);
            List<Zona> zonas = zonaService.obtenerZonasDisponibles();

            req.setAttribute("movimientos", movimientos);
            req.setAttribute("zonas", zonas);
            req.setAttribute("fechaInicio", fechaInicio);
            req.setAttribute("fechaFin", fechaFin);
            req.setAttribute("zonaIdSeleccionada", zonaId);
            req.setAttribute("tipoSeleccionado", tipo);

            req.getRequestDispatcher("/views/consulta_flujo.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al consultar el flujo vehicular", e);
        }
    }

    private String obtenerOPorDefecto(String valor, String porDefecto) {
        return (valor == null || valor.isEmpty()) ? porDefecto : valor;
    }
}
