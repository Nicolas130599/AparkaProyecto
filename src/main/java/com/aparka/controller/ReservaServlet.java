
package com.aparka.controller;

import com.aparka.dao.EntradaDAO;
import com.aparka.model.Entrada;
import com.aparka.model.UsuarioSistema;
import com.aparka.util.ValidacionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Registra el ingreso vehicular a una zona de APARKA.
 */
@WebServlet("/reserva")
public class ReservaServlet extends HttpServlet {

    private final EntradaDAO entradaDAO = new EntradaDAO();

    private UsuarioSistema verificarAcceso(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        HttpSession session = req.getSession(false);

        UsuarioSistema usuario = session != null
                ? (UsuarioSistema) session.getAttribute("usuario")
                : null;

        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }

        if (!"USUARIO".equals(session.getAttribute("rol"))) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permisos para acceder a esta funcionalidad."
            );
            return null;
        }

        return usuario;
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        if (verificarAcceso(req, resp) == null) {
            return;
        }

        req.getRequestDispatcher("/views/reserva.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        UsuarioSistema usuario = verificarAcceso(req, resp);

        if (usuario == null) {
            return;
        }

        String zonaTexto = req.getParameter("zonaId");
        String placaTexto = req.getParameter("placa");

        req.setAttribute("zonaIdIngresada", zonaTexto);
        req.setAttribute("placaIngresada", placaTexto);

        int zonaId;

        try {
            zonaId = Integer.parseInt(
                    zonaTexto == null ? "" : zonaTexto.trim()
            );

            if (zonaId <= 0) {
                mostrarError(
                        req,
                        resp,
                        "Selecciona una zona válida."
                );
                return;
            }

        } catch (NumberFormatException e) {
            mostrarError(
                    req,
                    resp,
                    "El identificador de la zona no es válido."
            );
            return;
        }

        if (!ValidacionUtil.esPlacaValida(placaTexto)) {
            mostrarError(
                    req,
                    resp,
                    "Ingresa una placa válida de 5 a 7 caracteres."
            );
            return;
        }

        String placa = placaTexto.trim()
                .toUpperCase(Locale.ROOT);

        Entrada entrada = new Entrada();

        entrada.setCentroComercialId(
                usuario.getCentroComercialId() != null
                        ? usuario.getCentroComercialId()
                        : 1
        );

        entrada.setCardNo(
                "U" + usuario.getUsuarioId() + "-"
                + System.currentTimeMillis() % 100000
        );

        entrada.setPuertaId(1);
        entrada.setUsuarioId(usuario.getUsuarioId());
        entrada.setZonaId(zonaId);
        entrada.setPlaca(placa);

        try {
            entradaDAO.registrarIngreso(entrada);

            resp.sendRedirect(
                    req.getContextPath() + "/zonas?reservado=1"
            );

        } catch (SQLException e) {

            if ("La zona está llena. No hay espacios disponibles."
                    .equals(e.getMessage())) {

                mostrarError(
                        req,
                        resp,
                        "La zona seleccionada está llena. "
                        + "Por favor, elige otra zona disponible."
                );
                return;
            }

            if ("La zona seleccionada no existe."
                    .equals(e.getMessage())) {

                mostrarError(
                        req,
                        resp,
                        "La zona seleccionada no existe."
                );
                return;
            }

            throw new ServletException(
                    "Error al registrar el ingreso vehicular.",
                    e
            );
        }
    }

    private void mostrarError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensaje)
            throws ServletException, IOException {

        req.setAttribute("error", mensaje);

        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        req.getRequestDispatcher("/views/reserva.jsp")
                .forward(req, resp);
    }
}
