package com.aparka.controller;

import com.aparka.dao.UsuarioSistemaDAO;
import com.aparka.model.UsuarioSistema;
import com.aparka.util.ValidacionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RegistroServlet.class);

    private final UsuarioSistemaDAO usuarioDAO = new UsuarioSistemaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String contrasena = req.getParameter("contrasena");
        String nombres = req.getParameter("nombres");

        // Validación en el backend con Guava + Apache Commons (no confiar solo en el HTML)
        try {
            ValidacionUtil.requerirTextoNoVacio(username, "Usuario");
            ValidacionUtil.requerirTextoNoVacio(nombres, "Nombre completo");

            if (!ValidacionUtil.esCorreoValido(email)) {
                req.setAttribute("error", "El correo ingresado no tiene un formato válido.");
                req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
                return;
            }
            if (!ValidacionUtil.esPasswordValida(contrasena, 6)) {
                req.setAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
                req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
                return;
            }
        } catch (IllegalArgumentException ex) {
            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
            return;
        }

        UsuarioSistema usuario = new UsuarioSistema();
        usuario.setUsername(username);
        usuario.setPasswordHash(contrasena);
        usuario.setNombres(nombres);
        usuario.setEmail(email);
        usuario.setCentroComercialId(1); // único centro comercial en esta versión

        try {
            boolean ok = usuarioDAO.registrar(usuario);
            if (ok) {
                log.info("Nuevo usuario registrado: username='{}'", username);
                resp.sendRedirect(req.getContextPath() + "/login?registrado=1");
            } else {
                log.warn("Registro fallido para username='{}'", username);
                req.setAttribute("error", "No se pudo completar el registro.");
                req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            log.error("Error de BD al registrar username='{}'", username, e);
            throw new ServletException("Error al registrar usuario", e);
        }
    }
}

