package com.aparka.controller;

import com.aparka.dao.EntradaDAO;
import com.aparka.dao.PuertaDAO;
import com.aparka.dao.SalidaDAO;
import com.aparka.model.Entrada;
import com.aparka.model.Puerta;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Pantalla de operador/administrador para registrar la salida de un vehículo: buscar por ticket/placa, confirmar. */
@WebServlet("/admin/salidas")
public class RegistrarSalidaServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RegistrarSalidaServlet.class);

    private final EntradaDAO entradaDAO = new EntradaDAO();
    private final SalidaDAO salidaDAO = new SalidaDAO();
    private final PuertaDAO puertaDAO = new PuertaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("puertas", puertaDAO.listarTodas());
            req.getRequestDispatcher("/views/registrar_salida.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al cargar la pantalla de salida", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");

        try {
            List<Puerta> puertas = puertaDAO.listarTodas();
            req.setAttribute("puertas", puertas);

            if ("buscar".equals(accion)) {
                String criterio = req.getParameter("criterio");
                Entrada encontrada = entradaDAO.buscarAbiertaPorTicketOPlaca(criterio.trim().toUpperCase());

                if (encontrada == null) {
                    req.setAttribute("error", "No se encontró un vehículo estacionado con ese ticket o placa.");
                } else {
                    req.setAttribute("entradaEncontrada", encontrada);
                }
                req.getRequestDispatcher("/views/registrar_salida.jsp").forward(req, resp);

            } else if ("confirmar".equals(accion)) {
                long ticket = Long.parseLong(req.getParameter("ticket"));
                int puertaSalidaId = Integer.parseInt(req.getParameter("puertaSalidaId"));

                Entrada entradaOriginal = entradaDAO.buscarAbiertaPorTicketOPlaca(String.valueOf(ticket));
                if (entradaOriginal == null) {
                    req.setAttribute("error", "El ticket ya no está activo (puede que ya se haya cerrado).");
                    req.getRequestDispatcher("/views/registrar_salida.jsp").forward(req, resp);
                    return;
                }

                salidaDAO.registrarSalida(entradaOriginal, puertaSalidaId);
                log.info("Salida registrada: ticket={}, placa='{}'", ticket, entradaOriginal.getPlaca());
                resp.sendRedirect(req.getContextPath() + "/admin/salidas?registrado=1");
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/salidas");
            }
        } catch (SQLException e) {
            log.error("Error al procesar salida (acción={})", accion, e);
            throw new ServletException("Error al procesar la salida", e);
        }
    }
}
