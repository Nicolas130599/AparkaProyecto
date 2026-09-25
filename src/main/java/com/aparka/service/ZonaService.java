package com.aparka.service;

import com.aparka.dao.ZonaDAO;
import com.aparka.model.Zona;

import java.sql.SQLException;
import java.util.List;

public class ZonaService {

    private final ZonaDAO zonaDAO = new ZonaDAO();

    public List<Zona> obtenerZonasDisponibles() throws SQLException {
        return zonaDAO.listarTodas();
    }

    /** Usado en el dashboard del administrador: ranking de mayor a menor flujo. */
    public List<Zona> obtenerRankingFlujoVehicular() throws SQLException {
        return zonaDAO.listarOrdenadasPorFlujo();
    }

    // Nota: ya no existe un contador manual de ocupación (ajustarOcupacion).
    // Ahora la ocupación se calcula en vivo contando filas de Entrada con
    // TieneSalida = 0 (ver ZonaDAO.listarTodas), así que siempre refleja
    // el estado real de la tabla Entrada sin necesidad de sincronizar un número aparte.

    public boolean crearZona(Zona z) throws SQLException {
        return zonaDAO.insertar(z);
    }

    public boolean actualizarZona(Zona z) throws SQLException {
        return zonaDAO.actualizar(z);
    }

    public boolean eliminarZona(int id) throws SQLException {
        return zonaDAO.eliminar(id);
    }
}
