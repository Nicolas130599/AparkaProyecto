package com.aparka.controller;

import com.aparka.dao.RecuperacionPasswordDAO;
import com.aparka.dao.UsuarioSistemaDAO;
import com.aparka.model.UsuarioSistema;
import com.aparka.util.EmailUtil;
import com.aparka.util.TokenRecuperacionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/recuperar")
public class RecuperarServlet extends HttpServlet {

    private static final Logger log =
            LoggerFactory.getLogger(RecuperarServlet.class);

    private static final String MENSAJE_GENERICO =
            "Si existe una cuenta activa asociada a los datos "
            + "ingresados, recibirÃ¡s un enlace de recuperaciÃ³n "
            + "en tu correo electrÃ³nico.";

    private final UsuarioSistemaDAO usuarioDAO =
            new UsuarioSistemaDAO();

    private final RecuperacionPasswordDAO recuperacionDAO =
            new RecuperacionPasswordDAO();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        configurarCabeceras(resp);
        mostrarFormulario(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        configurarCabeceras(resp);

        String identificador = req.getParameter("identificador");

        if (identificador == null
                || identificador.isBlank()
                || identificador.length() > 150) {

            mostrarMensajeGenerico(req, resp);
            return;
        }

        identificador = identificador.trim();

        String baseUrl = System.getenv("APARKA_BASE_URL");

        boolean urlLocalValida =
                ("http://localhost:8080/AparkaProyecto".equals(baseUrl) || "http://localhost:8081/AparkaProyecto".equals(baseUrl));

        boolean urlHttpsValida =
                baseUrl != null
                && baseUrl.matches(
                        "https://[A-Za-z0-9.-]+(?::443)?/AparkaProyecto/?"
                );

        if (!urlLocalValida && !urlHttpsValida) {

            log.error(
                    "APARKA_BASE_URL no configurada o no permitida"
            );

            mostrarMensajeGenerico(req, resp);
            return;
        }

        try {

            UsuarioSistema usuario =
                    usuarioDAO.buscarPorUsernameOEmail(identificador);

            if (usuario != null
                    && usuario.getEmail() != null
                    && !usuario.getEmail().isBlank()) {

                String token =
                        TokenRecuperacionUtil.generarToken();

                String tokenHash =
                        TokenRecuperacionUtil.calcularHash(token);

                boolean registrado =
                        recuperacionDAO.registrarToken(
                                usuario.getUsuarioId(),
                                tokenHash
                        );

                if (registrado) {

                    String enlace =
                            baseUrl.replaceAll("/+$", "")
                            + "/restablecer?token=" + token;

                    String asunto =
                            "APARKA - RecuperaciÃ³n de contraseÃ±a";

                    String nombre =
                            usuario.getNombres() == null
                            ? "usuario"
                            : usuario.getNombres();

                    String mensaje =
                            "Hola " + nombre + ",\n\n"
                            + "Recibimos una solicitud para restablecer "
                            + "tu contraseÃ±a de APARKA.\n\n"
                            + "Para continuar, abre este enlace:\n"
                            + enlace + "\n\n"
                            + "El enlace vence en 15 minutos y "
                            + "solo puede utilizarse una vez.\n\n"
                            + "Si no solicitaste este cambio, "
                            + "puedes ignorar este mensaje. "
                            + "Tu contraseÃ±a actual seguirÃ¡ funcionando.\n\n"
                            + "Atentamente,\n"
                            + "Equipo APARKA";

                    try {

                        EmailUtil.enviarCorreo(
                                usuario.getEmail(),
                                asunto,
                                mensaje
                        );

                        log.info(
                                "Solicitud de recuperaciÃ³n procesada"
                        );

                    } catch (Exception e) {

                        log.error(
                                "Error al enviar correo de recuperaciÃ³n",
                                e
                        );
                    }
                }
            }

        } catch (SQLException e) {

            log.error(
                    "Error de base de datos durante recuperaciÃ³n",
                    e
            );

        } catch (IllegalArgumentException e) {

            log.error(
                    "Error al generar token de recuperaciÃ³n",
                    e
            );
        }

        mostrarMensajeGenerico(req, resp);
    }

    private void mostrarMensajeGenerico(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("mensaje", MENSAJE_GENERICO);
        mostrarFormulario(req, resp);
    }

    private void configurarCabeceras(
            HttpServletResponse resp) {

        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Referrer-Policy", "no-referrer");
        resp.setHeader("X-Content-Type-Options", "nosniff");
    }

    private void mostrarFormulario(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/views/recuperar.jsp")
                .forward(req, resp);
    }
}
