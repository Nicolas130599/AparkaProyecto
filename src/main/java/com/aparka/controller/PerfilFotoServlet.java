package com.aparka.controller;

import com.aparka.dao.UsuarioSistemaDAO;
import com.aparka.model.UsuarioSistema;
import com.aparka.util.FotoPerfilUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Foto de perfil del usuario logueado.
 *  GET  /perfil/foto  -> entrega la foto propia (las fotos viven fuera de la carpeta web).
 *  POST /perfil/foto  -> accion=subir (multipart, campo "foto") o accion=eliminar.
 */
@WebServlet("/perfil/foto")
@MultipartConfig(
        fileSizeThreshold = 256 * 1024,
        maxFileSize = FotoPerfilUtil.MAX_BYTES,
        maxRequestSize = FotoPerfilUtil.MAX_BYTES + 64 * 1024
)
public class PerfilFotoServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PerfilFotoServlet.class);
    private final UsuarioSistemaDAO usuarioDAO = new UsuarioSistemaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UsuarioSistema u = session == null ? null : (UsuarioSistema) session.getAttribute("usuario");
        if (u == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Path archivo = FotoPerfilUtil.ruta(u.getUsuarioId());
        if (u.getFotoPerfil() == null || !Files.isRegularFile(archivo)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // El navegador la guarda pero pregunta si cambió (304 si no): así la foto nueva
        // aparece al instante sin tener que limpiar la caché.
        long modificado = Files.getLastModifiedTime(archivo).toMillis() / 1000 * 1000;
        long consulta = req.getDateHeader("If-Modified-Since");
        resp.setHeader("Cache-Control", "private, no-cache");
        resp.setDateHeader("Last-Modified", modificado);
        if (consulta >= modificado) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        resp.setContentType("image/jpeg");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setContentLengthLong(Files.size(archivo));
        Files.copy(archivo, resp.getOutputStream());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UsuarioSistema u = session == null ? null : (UsuarioSistema) session.getAttribute("usuario");
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            String accion = req.getParameter("accion");
            if ("subir".equals(accion)) {
                subir(req, session, u);
            } else if ("eliminar".equals(accion)) {
                eliminar(session, u);
            } else {
                PerfilServlet.flash(session, "error", "Acción no válida.");
            }
        } catch (SQLException e) {
            log.error("Error de BD al actualizar la foto del usuario id={}", u.getUsuarioId(), e);
            PerfilServlet.flash(session, "error", "No se pudo guardar la foto por un problema del sistema.");
        }
        resp.sendRedirect(req.getContextPath() + "/perfil");
    }

    private void subir(HttpServletRequest req, HttpSession session, UsuarioSistema u) throws SQLException {
        try {
            Part parte = req.getPart("foto");
            if (parte == null || parte.getSize() == 0) {
                PerfilServlet.flash(session, "error", "No seleccionaste ninguna imagen.");
                return;
            }
            String archivo;
            try (InputStream in = parte.getInputStream()) {
                archivo = FotoPerfilUtil.guardar(u.getUsuarioId(), in);
            }
            usuarioDAO.actualizarFoto(u.getUsuarioId(), archivo);
            session.setAttribute("usuario", usuarioDAO.obtenerPorId(u.getUsuarioId()));
            log.info("Foto de perfil actualizada: username='{}'", u.getUsername());
            PerfilServlet.flash(session, "exito", "Tu foto de perfil se actualizó correctamente.");
        } catch (IllegalArgumentException e) {
            // Mensajes pensados para el usuario (formato, tamaño, etc.)
            PerfilServlet.flash(session, "error", e.getMessage());
        } catch (IllegalStateException e) {
            // Tomcat lo lanza cuando el archivo supera maxFileSize
            PerfilServlet.flash(session, "error", "La imagen supera el máximo de 2 MB.");
        } catch (IOException | ServletException e) {
            log.error("No se pudo guardar la foto del usuario id={}", u.getUsuarioId(), e);
            PerfilServlet.flash(session, "error", "No se pudo guardar la foto. Inténtalo con otro archivo.");
        }
    }

    private void eliminar(HttpSession session, UsuarioSistema u) throws SQLException {
        try {
            FotoPerfilUtil.eliminar(u.getUsuarioId());
        } catch (IOException e) {
            log.warn("No se pudo borrar el archivo de la foto del usuario id={}", u.getUsuarioId(), e);
        }
        usuarioDAO.actualizarFoto(u.getUsuarioId(), null);
        session.setAttribute("usuario", usuarioDAO.obtenerPorId(u.getUsuarioId()));
        PerfilServlet.flash(session, "exito", "Tu foto de perfil fue eliminada.");
    }
}
