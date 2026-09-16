package com.aparka.controller;

import com.aparka.dao.ReservaDAO;
import com.aparka.model.Reserva;
import com.aparka.model.Usuario;
import com.aparka.service.ZonaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/reserva")
public class ReservaServlet extends HttpServlet {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final ZonaService zonaService = new ZonaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/reserva.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int zonaId = Integer.parseInt(req.getParameter("zonaId"));
        String placa = req.getParameter("placa");

        Reserva reserva = new Reserva();
        reserva.setUsuarioId(usuario.getId());
        reserva.setZonaId(zonaId);
        reserva.setPlaca(placa);

        try {
            reservaDAO.registrarIngreso(reserva);
            zonaService.registrarIngresoVehiculo(zonaId);
            resp.sendRedirect(req.getContextPath() + "/zonas?reservado=1");
        } catch (SQLException e) {
            throw new ServletException("Error al registrar la reserva", e);
        }
    }
}
