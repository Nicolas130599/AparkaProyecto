package com.aparka.controller;

import com.aparka.dao.UsuarioSistemaDAO;
import com.aparka.model.UsuarioSistema;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);

    private final UsuarioSistemaDAO usuarioDAO = new UsuarioSistemaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String contrasena = req.getParameter("contrasena");

        try {
            UsuarioSistema usuario = usuarioDAO.autenticar(username, contrasena);
            if (usuario != null) {
                HttpSession session = req.getSession();
                session.setAttribute("usuario", usuario);
                session.setAttribute("rol", usuario.getNombreRol());

                log.info("Login exitoso: username='{}', rol='{}', ip='{}'",
                        username, usuario.getNombreRol(), req.getRemoteAddr());

                if ("ADMIN".equals(usuario.getNombreRol())) {
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/zonas");
                }
            } else {
                // Evento de seguridad: intento de login fallido (útil para detectar fuerza bruta)
                log.warn("Login fallido: username='{}', ip='{}'", username, req.getRemoteAddr());
                req.setAttribute("error", "Usuario o contraseña incorrectos.");
                req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            log.error("Error de BD al autenticar username='{}'", username, e);
            throw new ServletException("Error al autenticar", e);
        }
    }
}
