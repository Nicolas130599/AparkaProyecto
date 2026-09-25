package com.aparka.controller;

import com.aparka.dao.UsuarioSistemaDAO;
import com.aparka.util.ValidacionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Recuperación de contraseña simplificada para el alcance académico:
 * el usuario confirma su Username + Email registrados, y define una
 * contraseña nueva directamente (sin envío real de correo).
 */
@WebServlet("/recuperar")
public class RecuperarServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RecuperarServlet.class);

    private final UsuarioSistemaDAO usuarioDAO = new UsuarioSistemaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/recuperar.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String nuevaContrasena = req.getParameter("nuevaContrasena");

        if (!ValidacionUtil.esPasswordValida(nuevaContrasena, 6)) {
            req.setAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres.");
            req.getRequestDispatcher("/views/recuperar.jsp").forward(req, resp);
            return;
        }

        try {
            boolean coincide = usuarioDAO.verificarUsuarioYCorreo(username, email);

            if (!coincide) {
                // Evento de seguridad: intento de recuperación con datos que no coinciden
                log.warn("Intento de recuperación fallido: username='{}', ip='{}'", username, req.getRemoteAddr());
                req.setAttribute("error", "El usuario y el correo no coinciden con ninguna cuenta activa.");
                req.getRequestDispatcher("/views/recuperar.jsp").forward(req, resp);
                return;
            }

            usuarioDAO.actualizarPassword(username, nuevaContrasena);
            log.info("Contraseña actualizada vía recuperación: username='{}', ip='{}'", username, req.getRemoteAddr());
            resp.sendRedirect(req.getContextPath() + "/login?passwordActualizada=1");

        } catch (SQLException e) {
            log.error("Error de BD en recuperación de contraseña username='{}'", username, e);
            throw new ServletException("Error al recuperar la contraseña", e);
        }
    }
}
