package com.aparka.dao;

import com.aparka.model.Puerta;
import com.aparka.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PuertaDAO {

    public List<Puerta> listarTodas() throws SQLException {
        List<Puerta> puertas = new ArrayList<>();
        String sql = "SELECT * FROM PuertaMaster ORDER BY Descripcion";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Puerta p = new Puerta();
                p.setPuertaId(rs.getInt("PuertaId"));
                p.setDescripcion(rs.getString("Descripcion"));
                p.setTipo(rs.getString("Tipo"));
                puertas.add(p);
            }
        }
        return puertas;
    }
}
