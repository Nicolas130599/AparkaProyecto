package com.aparka.controller;

import com.aparka.dao.UsuarioSistemaDAO;
import com.aparka.model.UsuarioSistema;
import com.aparka.util.PasswordGenerator;
import com.aparka.util.EmailUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/recuperar")
public class RecuperarServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RecuperarServlet.class);
    private final UsuarioSistemaDAO usuarioDAO = new UsuarioSistemaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/views/recuperar.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String identificador = req.getParameter("identificador");

        try {
            // 1. Buscar al usuario por Username o Email en la base de datos
            UsuarioSistema usuario = usuarioDAO.buscarPorUsernameOEmail(identificador);

            if (usuario == null) {
                log.warn("Intento de recuperación para usuario/correo inexistente: '{}'", identificador);
                req.setAttribute("error", "No se encontró ninguna cuenta activa con ese usuario o correo.");
                req.getRequestDispatcher("/views/recuperar.jsp").forward(req, resp);
                return;
            }

            // 2. Generar contraseña temporal única y diferente cada vez
            String passwordTemporal = PasswordGenerator.generarPasswordTemporal();

            // 3. Actualizar la contraseña en la BD (el DAO aplica jBCrypt internamente)
            boolean actualizado = usuarioDAO.actualizarPassword(usuario.getUsername(), passwordTemporal);

            if (actualizado) {
                // 4. Enviar el correo electrónico con la nueva contraseña generada
                String asunto = "Aparka - Recuperación de Contraseña";
                String mensaje = "Hola " + usuario.getNombres() + ",\n\n" +
                                 "Has solicitado restablecer tu contraseña en Aparka.\n" +
                                 "Tu nueva contraseña temporal de acceso es: " + passwordTemporal + "\n\n" +
                                 "Te recomendamos iniciar sesión con ella y cambiarla posteriormente.\n\n" +
                                 "Atentamente,\nEquipo de Soporte Aparka";

                try {
                    EmailUtil.enviarCorreo(usuario.getEmail(), asunto, mensaje);
                    log.info("Contraseña temporal generada y enviada por correo para el usuario: '{}'", usuario.getUsername());
                    req.setAttribute("mensaje", "Se ha enviado una nueva contraseña temporal a tu correo electrónico registrado.");
                } catch (Exception ex) {
                    // Imprimimos la traza completa en la consola para depurar el error exacto de correo
                    ex.printStackTrace();
                    log.error("Error detallado al enviar el correo electrónico a '{}': {}", usuario.getEmail(), ex.getMessage(), ex);
                    
                    req.setAttribute("error", "La contraseña se actualizó en el sistema, pero falló el envío del correo: " + ex.getMessage());
                }
            } else {
                req.setAttribute("error", "No se pudo actualizar la contraseña en el sistema.");
            }

            req.getRequestDispatcher("/views/recuperar.jsp").forward(req, resp);

        } catch (SQLException e) {
            log.error("Error de base de datos en recuperación para: '{}'", identificador, e);
            throw new ServletException("Error al procesar la recuperación de contraseña", e);
        }
    }
}