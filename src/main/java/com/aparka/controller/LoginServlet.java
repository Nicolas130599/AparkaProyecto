
package com.aparka.controller;

import com.aparka.dao.UsuarioSistemaDAO;
import com.aparka.model.UsuarioSistema;
import com.aparka.util.LoginAttemptLimiter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger log =
            LoggerFactory.getLogger(LoginServlet.class);

    private final UsuarioSistemaDAO usuarioDAO =
            new UsuarioSistemaDAO();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/views/login.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String contrasena = req.getParameter("contrasena");
        String ip = req.getRemoteAddr();

        // Comprobar si se superó el límite de intentos.
        if (LoginAttemptLimiter.estaBloqueado(username, ip)) {

            log.warn("Intento de login limitado: ip='{}'", ip);

            resp.setStatus(429);

            req.setAttribute(
                    "error",
                    "Demasiados intentos de acceso. Inténtalo más tarde."
            );

            req.getRequestDispatcher("/views/login.jsp")
                    .forward(req, resp);

            return;
        }

        try {

            UsuarioSistema usuario =
                    usuarioDAO.autenticar(username, contrasena);

            if (usuario != null) {

                // Limpiar los intentos fallidos de este usuario e IP.
                LoginAttemptLimiter.registrarExito(username, ip);

                HttpSession session = req.getSession();

                // Protección contra fijación de sesión.
                req.changeSessionId();

                session.setAttribute("usuario", usuario);
                session.setAttribute("rol", usuario.getNombreRol());

                log.info(
                        "Login exitoso: username='{}', rol='{}', ip='{}'",
                        username,
                        usuario.getNombreRol(),
                        ip
                );

                if ("ADMIN".equals(usuario.getNombreRol())) {

                    resp.sendRedirect(
                            req.getContextPath() + "/admin/dashboard"
                    );

                } else {

                    resp.sendRedirect(
                            req.getContextPath() + "/zonas"
                    );
                }

            } else {

                // Registrar el intento de autenticación fallido.
                LoginAttemptLimiter.registrarFallo(username, ip);

                log.warn(
                        "Login fallido: username='{}', ip='{}'",
                        username,
                        ip
                );

                req.setAttribute(
                        "error",
                        "Usuario o contraseña incorrectos."
                );

                req.getRequestDispatcher("/views/login.jsp")
                        .forward(req, resp);
            }

        } catch (SQLException e) {

            log.error("Error de BD al autenticar", e);

            throw new ServletException(
                    "Error al autenticar", e
            );
        }
    }
}
