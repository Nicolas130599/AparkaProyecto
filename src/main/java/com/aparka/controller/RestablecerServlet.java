
package com.aparka.controller;

import com.aparka.dao.RecuperacionPasswordDAO;
import com.aparka.util.TokenRecuperacionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/restablecer")
public class RestablecerServlet extends HttpServlet {

    private static final Logger log =
            LoggerFactory.getLogger(RestablecerServlet.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RecuperacionPasswordDAO recuperacionDAO =
            new RecuperacionPasswordDAO();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        configurarCabeceras(resp);

        String token = req.getParameter("token");

        if (!tokenValido(token)) {
            req.setAttribute("error",
                    "El enlace de recuperación no es válido.");
            mostrarFormulario(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        String csrfToken = obtenerOCrearCsrf(session);

        req.setAttribute("token", token);
        req.setAttribute("csrfToken", csrfToken);

        mostrarFormulario(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        configurarCabeceras(resp);

        HttpSession session = req.getSession(false);

        String csrfEsperado = session == null
                ? null
                : (String) session.getAttribute("csrfToken");

        String csrfRecibido = req.getParameter("csrfToken");

        if (!coinciden(csrfEsperado, csrfRecibido)) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Solicitud rechazada por seguridad CSRF");
            return;
        }

        String token = req.getParameter("token");
        String nuevaPassword = req.getParameter("nuevaPassword");
        String confirmarPassword = req.getParameter("confirmarPassword");

        if (!tokenValido(token)) {
            req.setAttribute("error",
                    "El enlace de recuperación no es válido.");
            mostrarFormulario(req, resp);
            return;
        }

        if (nuevaPassword == null
                || confirmarPassword == null
                || nuevaPassword.length() < 8
                || nuevaPassword.length() > 128
                || !nuevaPassword.equals(confirmarPassword)) {

            req.setAttribute("error",
                    "Las contraseñas deben coincidir y tener "
                    + "entre 8 y 128 caracteres.");

            req.setAttribute("token", token);
            req.setAttribute("csrfToken", csrfEsperado);

            mostrarFormulario(req, resp);
            return;
        }

        try {
            boolean actualizado =
                    recuperacionDAO.consumirTokenYCambiarPassword(
                            token, nuevaPassword);

            if (actualizado) {
                session.removeAttribute("csrfToken");

                req.setAttribute("mensaje",
                        "Tu contraseña fue actualizada correctamente. "
                        + "Ya puedes iniciar sesión.");

            } else {
                req.setAttribute("error",
                        "El enlace de recuperación ha vencido, "
                        + "ya fue utilizado o no es válido.");
            }

        } catch (SQLException e) {
            log.error("Error al restablecer contraseña", e);

            req.setAttribute("error",
                    "No fue posible completar la solicitud. "
                    + "Inténtalo nuevamente.");
        }

        mostrarFormulario(req, resp);
    }

    private boolean tokenValido(String token) {
        try {
            TokenRecuperacionUtil.calcularHash(token);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String obtenerOCrearCsrf(HttpSession session) {

        synchronized (session) {
            String token = (String) session.getAttribute("csrfToken");

            if (token == null) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);

                token = Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

                session.setAttribute("csrfToken", token);
            }

            return token;
        }
    }

    private boolean coinciden(String esperado, String recibido) {

        if (esperado == null || recibido == null) {
            return false;
        }

        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
    }

    private void configurarCabeceras(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Referrer-Policy", "no-referrer");
        resp.setHeader("X-Content-Type-Options", "nosniff");
    }

    private void mostrarFormulario(HttpServletRequest req,
                                   HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/views/restablecer.jsp")
                .forward(req, resp);
    }
}
