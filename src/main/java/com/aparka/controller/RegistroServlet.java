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
        
        // CAPTURANDO DOCUMENTO Y TELÉFONO DESDE EL FORMULARIO
        String documento = req.getParameter("documento");
        String telefono = req.getParameter("telefono");

        // Validación en el backend con Guava + Apache Commons (no confiar solo en el HTML)
        try {
            ValidacionUtil.requerirTextoNoVacio(username, "Usuario");
            ValidacionUtil.requerirTextoNoVacio(nombres, "Nombre completo");
            ValidacionUtil.requerirTextoNoVacio(documento, "Documento de identidad");

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
        usuario.setNombres(nombres);
        usuario.setEmail(email);
        
        // ASIGNANDO DOCUMENTO Y TELÉFONO AL OBJETO
        usuario.setDocumento(documento);
        usuario.setTelefono(telefono);
        
        usuario.setCentroComercialId(1); // único centro comercial en esta versión

        try {
            // Invocamos el método del DAO pasando el objeto y la contraseña plana para encriptarla con jBCrypt
            boolean ok = usuarioDAO.registrar(usuario, contrasena);
            if (ok) {
                log.info("Nuevo usuario registrado con éxito y contraseña cifrada: username='{}'", username);
                resp.sendRedirect(req.getContextPath() + "/login?registrado=1");
            } else {
                log.warn("Registro fallido para username='{}'", username);
                req.setAttribute("error", "No se pudo completar el registro.");
                req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            log.error("Error de BD al registrar username='{}'", username, e);
            
            // Detección inteligente de campos duplicados (Username, Email, Documento)
            String mensajeError = "Error al registrar usuario en la base de datos.";
            String errorMsg = e.getMessage().toLowerCase();
            
            if (errorMsg.contains("duplicate entry")) {
                if (errorMsg.contains("username")) {
                    mensajeError = "El nombre de usuario ya se encuentra registrado. Por favor, elige otro.";
                } else if (errorMsg.contains("email")) {
                    mensajeError = "El correo electrónico ingresado ya está registrado.";
                } else if (errorMsg.contains("documento")) {
                    mensajeError = "El número de documento ingresado ya está registrado.";
                } else {
                    mensajeError = "Uno de los datos ingresados (usuario, correo o documento) ya existe en el sistema.";
                }
            }

            req.setAttribute("error", mensajeError);
            req.getRequestDispatcher("/views/registro.jsp").forward(req, resp);
        }
    }
}