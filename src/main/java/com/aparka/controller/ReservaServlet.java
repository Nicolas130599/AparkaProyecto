package com.aparka.controller;

import com.aparka.dao.EntradaDAO;
import com.aparka.model.Entrada;
import com.aparka.model.UsuarioSistema;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/** Registra el ingreso de un vehículo a una zona (tabla Entrada). */
@WebServlet("/reserva")
public class ReservaServlet extends HttpServlet {

    private final EntradaDAO entradaDAO = new EntradaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/reserva.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UsuarioSistema usuario = (session != null) ? (UsuarioSistema) session.getAttribute("usuario") : null;

        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int zonaId = Integer.parseInt(req.getParameter("zonaId"));
        String placa = req.getParameter("placa");

        Entrada entrada = new Entrada();
        entrada.setCentroComercialId(usuario.getCentroComercialId() != null ? usuario.getCentroComercialId() : 1);
        entrada.setCardNo("U" + usuario.getUsuarioId() + "-" + System.currentTimeMillis() % 100000);
        entrada.setPuertaId(1); // puerta por defecto en esta versión académica
        entrada.setUsuarioId(usuario.getUsuarioId());
        entrada.setZonaId(zonaId);
        entrada.setPlaca(placa);

        try {
            entradaDAO.registrarIngreso(entrada);
            resp.sendRedirect(req.getContextPath() + "/zonas?reservado=1");
        } catch (SQLException e) {
            throw new ServletException("Error al registrar el ingreso", e);
        }
    }
}
