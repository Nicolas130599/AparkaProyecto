package com.aparka.dao;

import com.aparka.model.Usuario;
import com.aparka.util.ConexionBD;

import java.sql.*;

public class UsuarioDAO {

    /** Valida credenciales y devuelve el usuario si son correctas, o null. */
    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE correo = ? AND contrasena = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasena); // En producción: comparar hash (BCrypt), no texto plano
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** Registra un nuevo usuario con rol USUARIO por defecto. */
    public boolean registrar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, correo, contrasena, placa, rol) VALUES (?,?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());
            ps.setString(3, usuario.getContrasena());
            ps.setString(4, usuario.getPlaca());
            ps.setString(5, "USUARIO");
            return ps.executeUpdate() > 0;
        }
    }

    public Usuario obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setPlaca(rs.getString("placa"));
        u.setRol(rs.getString("rol"));
        return u;
    }
}
