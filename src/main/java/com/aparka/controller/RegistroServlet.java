package com.aparka.controller;

import com.aparka.dao.UsuarioDAO;
import com.aparka.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario usuario = new Usuario();
        usuario.setNombre(req.getParameter("nombre"));
        usuario.setCorreo(req.getParameter("correo"));
        usuario.setContrasena(req.getParameter("contrasena"));
        usuario.setPlaca(req.getParameter("placa"));

        try {
            boolean ok = usuarioDAO.registrar(usuario);
            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/login?registrado=1");
            } else {
                req.setAttribute("error", "No se pudo completar el registro.");
                req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException("Error al registrar usuario", e);
        }
    }
}
