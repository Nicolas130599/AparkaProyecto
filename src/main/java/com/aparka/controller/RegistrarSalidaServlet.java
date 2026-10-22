
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

/**
 * Pantalla de operador/administrador para registrar
 * la salida de un vehículo.
 *
 * Permite buscar por ticket o placa y confirmar la salida.
 * Evita confirmar dos veces la salida del mismo ingreso.
 */
@WebServlet("/admin/salidas")
public class RegistrarSalidaServlet extends HttpServlet {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrarSalidaServlet.class);

    private final EntradaDAO entradaDAO = new EntradaDAO();
    private final SalidaDAO salidaDAO = new SalidaDAO();
    private final PuertaDAO puertaDAO = new PuertaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            req.setAttribute("puertas", puertaDAO.listarTodas());

            req.getRequestDispatcher("/views/registrar_salida.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al cargar la pantalla de salida", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");

        try {
            List<Puerta> puertas = puertaDAO.listarTodas();
            req.setAttribute("puertas", puertas);

            // 1. Buscar un vehículo estacionado.
            if ("buscar".equals(accion)) {

                String criterio = req.getParameter("criterio");

                if (criterio == null || criterio.isBlank()) {
                    req.setAttribute(
                            "error",
                            "Ingresa un número de ticket o una placa."
                    );

                    mostrarFormulario(req, resp);
                    return;
                }

                Entrada encontrada =
                        entradaDAO.buscarAbiertaPorTicketOPlaca(
                                criterio.trim().toUpperCase()
                        );

                if (encontrada == null) {

                    req.setAttribute(
                            "error",
                            "No se encontró un vehículo estacionado "
                            + "con ese ticket o placa."
                    );

                } else {

                    req.setAttribute(
                            "entradaEncontrada",
                            encontrada
                    );
                }

                mostrarFormulario(req, resp);
                return;
            }

            // 2. Confirmar la salida del vehículo.
            if ("confirmar".equals(accion)) {

                long ticket;
                int puertaSalidaId;

                try {
                    ticket = Long.parseLong(
                            req.getParameter("ticket")
                    );

                    puertaSalidaId = Integer.parseInt(
                            req.getParameter("puertaSalidaId")
                    );

                    if (ticket <= 0 || puertaSalidaId <= 0) {
                        throw new NumberFormatException(
                                "Identificadores inválidos"
                        );
                    }

                } catch (NumberFormatException e) {

                    req.setAttribute(
                            "error",
                            "El ticket o la puerta de salida "
                            + "no son válidos."
                    );

                    resp.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST
                    );

                    mostrarFormulario(req, resp);
                    return;
                }

                // Comprobar que el ticket esté activo.
                Entrada entradaOriginal =
                        entradaDAO.buscarAbiertaPorTicketOPlaca(
                                String.valueOf(ticket)
                        );

                if (entradaOriginal == null) {

                    req.setAttribute(
                            "error",
                            "El ticket ya no está activo "
                            + "o su salida ya fue registrada."
                    );

                    mostrarFormulario(req, resp);
                    return;
                }

                // El DAO realiza la operación de forma transaccional.
                boolean salidaRegistrada =
                        salidaDAO.registrarSalida(
                                entradaOriginal,
                                puertaSalidaId
                        );

                // Puede devolver false si otra solicitud
                // cerró el mismo ticket simultáneamente.
                if (!salidaRegistrada) {

                    log.warn(
                            "Salida duplicada rechazada: ticket={}",
                            ticket
                    );

                    req.setAttribute(
                            "error",
                            "Este ticket ya tiene una salida registrada "
                            + "o no se encuentra activo."
                    );

                    resp.setStatus(
                            HttpServletResponse.SC_CONFLICT
                    );

                    mostrarFormulario(req, resp);
                    return;
                }

                log.info(
                        "Salida registrada: ticket={}, placa='{}'",
                        ticket,
                        entradaOriginal.getPlaca()
                );

                resp.sendRedirect(
                        req.getContextPath()
                        + "/admin/salidas?registrado=1"
                );

                return;
            }

            // 3. Acción no reconocida.
            resp.sendRedirect(
                    req.getContextPath() + "/admin/salidas"
            );

        } catch (SQLException e) {

            log.error(
                    "Error al procesar salida (acción={})",
                    accion,
                    e
            );

            throw new ServletException(
                    "Error al procesar la salida", e
            );
        }
    }

    /**
     * Muestra el formulario conservando los atributos
     * y mensajes de la solicitud.
     */
    private void mostrarFormulario(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/views/registrar_salida.jsp")
                .forward(req, resp);
    }
}
