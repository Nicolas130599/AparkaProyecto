
package com.aparka.controller;

import com.aparka.dao.EntradaDAO;
import com.aparka.dao.PuertaDAO;
import com.aparka.model.Entrada;
import com.aparka.model.Puerta;
import com.aparka.model.Zona;
import com.aparka.service.ZonaService;
import com.aparka.util.ValidacionUtil;

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
 * el ingreso de cualquier vehículo.
 */
@WebServlet("/admin/entradas")
public class RegistrarEntradaServlet extends HttpServlet {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrarEntradaServlet.class);

    private final EntradaDAO entradaDAO = new EntradaDAO();
    private final PuertaDAO puertaDAO = new PuertaDAO();
    private final ZonaService zonaService = new ZonaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            List<Puerta> puertas = puertaDAO.listarTodas();
            List<Zona> zonas = zonaService.obtenerZonasDisponibles();

            req.setAttribute("puertas", puertas);
            req.setAttribute("zonas", zonas);

            req.getRequestDispatcher("/views/registrar_entrada.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al cargar el formulario de entrada", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String placa = req.getParameter("placa");

        // Validación de placa
        if (!ValidacionUtil.esPlacaValida(placa)) {
            req.setAttribute("error",
                    "Ingresa una placa válida (5 a 8 caracteres).");

            doGet(req, resp);
            return;
        }

        Entrada entrada = new Entrada();

        entrada.setCentroComercialId(1);
        entrada.setCardNo(
                "OP-" + System.currentTimeMillis() % 1000000);

        entrada.setPuertaId(
                Integer.parseInt(req.getParameter("puertaId")));

        entrada.setZonaId(
                Integer.parseInt(req.getParameter("zonaId")));

        entrada.setPlaca(placa.trim().toUpperCase());

        // Registrado por el operador
        entrada.setUsuarioId(null);

        String tipoClienteParam =
                req.getParameter("tipoClienteId");

        if (tipoClienteParam != null
                && !tipoClienteParam.isEmpty()) {

            entrada.setTipoClienteId(
                    Integer.parseInt(tipoClienteParam));
        }

        try {

            // Registrar ingreso
            long ticket = entradaDAO.registrarIngreso(entrada);

            log.info(
                    "Ingreso registrado por operador: ticket={}, placa='{}', zonaId={}",
                    ticket,
                    entrada.getPlaca(),
                    entrada.getZonaId()
            );

            resp.sendRedirect(
                    req.getContextPath()
                    + "/admin/entradas?registrado=1&ticket="
                    + ticket
            );

        } catch (SQLException e) {

            // Error MySQL 1062: clave única duplicada.
            // Se identifica específicamente la restricción
            // que impide dos ingresos activos de una misma placa.
            if (e.getErrorCode() == 1062
                    && e.getMessage() != null
                    && e.getMessage().contains(
                            "uq_entrada_placa_activa")) {

                log.warn(
                        "Ingreso duplicado rechazado: placa='{}'",
                        entrada.getPlaca()
                );

                resp.setStatus(
                        HttpServletResponse.SC_CONFLICT);

                req.setAttribute(
                        "error",
                        "Este vehículo ya tiene un ingreso activo. "
                        + "Debes registrar su salida antes "
                        + "de generar otro ticket."
                );

                doGet(req, resp);
                return;
            }

            // Otros errores de base de datos
            log.error(
                    "Error al registrar ingreso desde admin, placa='{}'",
                    entrada.getPlaca(),
                    e
            );

            throw new ServletException(
                    "Error al registrar el ingreso", e);
        }
    }
}
