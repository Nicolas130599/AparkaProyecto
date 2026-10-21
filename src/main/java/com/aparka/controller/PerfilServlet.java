package com.aparka.controller;

import com.aparka.dao.UsuarioSistemaDAO;
import com.aparka.model.UsuarioSistema;
import com.aparka.util.EmailUtil;
import com.aparka.util.ValidacionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Perfil del usuario logueado (cualquier rol): ver y editar sus datos de contacto
 * y cambiar su contraseña. La foto se gestiona en {@link PerfilFotoServlet}.
 *
 * Usuario, documento y rol son de solo lectura. Cambiar el correo exige confirmar
 * la contraseña actual, porque el correo es el canal de recuperación de la cuenta.
 */
@WebServlet("/perfil")
public class PerfilServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PerfilServlet.class);
    private static final int LARGO_MIN_PASSWORD = 6;

    private final UsuarioSistemaDAO usuarioDAO = new UsuarioSistemaDAO();

    // ---------- mensajes "flash": se guardan en sesión y se muestran una sola vez ----------
    static void flash(HttpSession session, String tipo, String texto) {
        session.setAttribute("flashTipo", tipo);   // "exito" | "error"
        session.setAttribute("flashTexto", texto);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UsuarioSistema sesion = usuarioEnSesion(session);
        if (sesion == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            // Se recarga desde la BD para mostrar siempre los datos vigentes
            UsuarioSistema actual = usuarioDAO.obtenerPorId(sesion.getUsuarioId());
            if (actual == null) {
                session.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            session.setAttribute("usuario", actual);
        } catch (SQLException e) {
            log.error("Error de BD al cargar el perfil del usuario id={}", sesion.getUsuarioId(), e);
            throw new ServletException("Error al cargar el perfil", e);
        }

        // Pasa el mensaje flash (si hay) a la petición y lo borra de la sesión
        if (session.getAttribute("flashTexto") != null) {
            req.setAttribute("mensajeTipo", session.getAttribute("flashTipo"));
            req.setAttribute("mensajeTexto", session.getAttribute("flashTexto"));
            session.removeAttribute("flashTipo");
            session.removeAttribute("flashTexto");
        }
        req.getRequestDispatcher("/views/perfil.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        UsuarioSistema sesion = usuarioEnSesion(session);
        if (sesion == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String accion = req.getParameter("accion");
        try {
            if ("datos".equals(accion)) {
                guardarDatos(req, session, sesion);
            } else if ("password".equals(accion)) {
                cambiarPassword(req, session, sesion);
            } else {
                flash(session, "error", "Acción no válida.");
            }
        } catch (SQLException e) {
            log.error("Error de BD al actualizar el perfil del usuario id={}", sesion.getUsuarioId(), e);
            flash(session, "error", "No se pudo guardar por un problema del sistema. Inténtalo nuevamente.");
        }
        resp.sendRedirect(req.getContextPath() + "/perfil"); // PRG: evita reenviar el formulario al recargar
    }

    private void guardarDatos(HttpServletRequest req, HttpSession session, UsuarioSistema sesion)
            throws SQLException {
        String nombres = limpiar(req.getParameter("nombres"));
        String telefono = limpiar(req.getParameter("telefono"));
        String email = limpiar(req.getParameter("email"));
        String passwordActual = req.getParameter("passwordActual");

        if (nombres.isEmpty() || nombres.length() > 150) {
            flash(session, "error", "El nombre completo es obligatorio (máximo 150 caracteres).");
            return;
        }
        if (!ValidacionUtil.esTelefonoValido(telefono)) {
            flash(session, "error", "El teléfono no es válido. Usa entre 7 y 15 dígitos (ej. +51 987 654 321).");
            return;
        }
        if (!ValidacionUtil.esCorreoValido(email) || email.length() > 100) {
            flash(session, "error", "El correo ingresado no tiene un formato válido.");
            return;
        }

        String emailAnterior = sesion.getEmail();
        boolean cambioCorreo = emailAnterior == null || !email.equalsIgnoreCase(emailAnterior);

        if (cambioCorreo) {
            // El correo es el canal de recuperación: se pide la contraseña actual para cambiarlo
            if (passwordActual == null || passwordActual.isEmpty()
                    || usuarioDAO.autenticar(sesion.getUsername(), passwordActual) == null) {
                log.warn("Cambio de correo rechazado (contraseña incorrecta): username='{}'", sesion.getUsername());
                flash(session, "error", "Para cambiar el correo debes confirmar tu contraseña actual correctamente.");
                return;
            }
            if (usuarioDAO.emailEnUsoPorOtro(email, sesion.getUsuarioId())) {
                flash(session, "error", "Ese correo ya está asociado a otra cuenta.");
                return;
            }
        }

        usuarioDAO.actualizarPerfil(sesion.getUsuarioId(), nombres, email, telefono);
        session.setAttribute("usuario", usuarioDAO.obtenerPorId(sesion.getUsuarioId()));
        log.info("Perfil actualizado: username='{}', correoCambiado={}", sesion.getUsername(), cambioCorreo);

        if (cambioCorreo && emailAnterior != null && !emailAnterior.isBlank()) {
            avisarCambioDeCorreo(sesion, emailAnterior, email);
        }
        flash(session, "exito", "Tus datos se actualizaron correctamente.");
    }

    private void cambiarPassword(HttpServletRequest req, HttpSession session, UsuarioSistema sesion)
            throws SQLException {
        String actual = req.getParameter("passwordActual");
        String nueva = req.getParameter("passwordNueva");
        String confirma = req.getParameter("passwordConfirma");

        if (actual == null || actual.isEmpty()
                || usuarioDAO.autenticar(sesion.getUsername(), actual) == null) {
            log.warn("Cambio de contraseña rechazado (contraseña actual incorrecta): username='{}'", sesion.getUsername());
            flash(session, "error", "La contraseña actual no es correcta.");
            return;
        }
        if (!ValidacionUtil.esPasswordValida(nueva, LARGO_MIN_PASSWORD)) {
            flash(session, "error", "La nueva contraseña debe tener al menos " + LARGO_MIN_PASSWORD + " caracteres.");
            return;
        }
        if (!nueva.equals(confirma)) {
            flash(session, "error", "La confirmación no coincide con la nueva contraseña.");
            return;
        }
        if (nueva.equals(actual)) {
            flash(session, "error", "La nueva contraseña debe ser distinta a la actual.");
            return;
        }

        usuarioDAO.actualizarPassword(sesion.getUsername(), nueva);
        log.info("Contraseña cambiada desde el perfil: username='{}'", sesion.getUsername());
        flash(session, "exito", "Tu contraseña se actualizó correctamente.");
    }

    /** Aviso de seguridad al correo anterior. Si falla el envío, no se bloquea el cambio. */
    private void avisarCambioDeCorreo(UsuarioSistema u, String anterior, String nuevo) {
        String asunto = "Aparka - Se cambió el correo de tu cuenta";
        String mensaje = "Hola " + u.getNombres() + ",\n\n" +
                "El correo asociado a tu cuenta de Aparka fue cambiado a: " + nuevo + "\n\n" +
                "Si fuiste tú, no necesitas hacer nada. Si no reconoces este cambio, " +
                "comunícate de inmediato con el equipo de soporte.\n\n" +
                "Atentamente,\nEquipo de Soporte Aparka";
        try {
            EmailUtil.enviarCorreo(anterior, asunto, mensaje);
        } catch (Exception e) {
            log.warn("No se pudo enviar el aviso de cambio de correo a '{}': {}", anterior, e.getMessage());
        }
    }

    private static UsuarioSistema usuarioEnSesion(HttpSession session) {
        return session == null ? null : (UsuarioSistema) session.getAttribute("usuario");
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
