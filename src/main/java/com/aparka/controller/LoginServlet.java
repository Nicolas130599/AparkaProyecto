package com.aparka.controller;

import com.aparka.dao.UsuarioDAO;
import com.aparka.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String correo = req.getParameter("correo");
        String contrasena = req.getParameter("contrasena");

        try {
            Usuario usuario = usuarioDAO.autenticar(correo, contrasena);
            if (usuario != null) {
                HttpSession session = req.getSession();
                session.setAttribute("usuario", usuario);
                session.setAttribute("rol", usuario.getRol());

                if ("ADMIN".equals(usuario.getRol())) {
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/zonas");
                }
            } else {
                req.setAttribute("error", "Correo o contraseña incorrectos.");
                req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException("Error al autenticar", e);
        }
    }
}
