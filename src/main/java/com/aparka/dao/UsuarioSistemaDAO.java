package com.aparka.dao;

import com.aparka.model.UsuarioSistema;
import com.aparka.util.ConexionBD;

import java.sql.*;

public class UsuarioSistemaDAO {

    /** Autentica contra UsuarioSistema, trayendo el nombre del Rol con un JOIN. */
    public UsuarioSistema autenticar(String username, String password) throws SQLException {
        String sql = "SELECT u.*, r.NombreRol " +
                "FROM UsuarioSistema u " +
                "JOIN Rol r ON u.RolId = r.RolId " +
                "WHERE u.Username = ? AND u.PasswordHash = ? AND u.Activo = 1";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password); // texto plano por simplicidad académica (ver README)
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    /** Registra un nuevo usuario con rol USUARIO (RolId = 2) por defecto. */
    public boolean registrar(UsuarioSistema u) throws SQLException {
        String sql = "INSERT INTO UsuarioSistema (Username, PasswordHash, Nombres, Email, RolId, CentroComercialId) " +
                "VALUES (?, ?, ?, ?, 2, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getNombres());
            ps.setString(4, u.getEmail());
            ps.setInt(5, u.getCentroComercialId() != null ? u.getCentroComercialId() : 1);
            return ps.executeUpdate() > 0;
        }
    }

    public UsuarioSistema obtenerPorId(int id) throws SQLException {
        String sql = "SELECT u.*, r.NombreRol FROM UsuarioSistema u " +
                "JOIN Rol r ON u.RolId = r.RolId WHERE u.UsuarioId = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    /** Verifica que el username y el email coincidan con un usuario activo (validación previa al reseteo). */
    public boolean verificarUsuarioYCorreo(String username, String email) throws SQLException {
        String sql = "SELECT UsuarioId FROM UsuarioSistema WHERE Username = ? AND Email = ? AND Activo = 1";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Actualiza la contraseña de un usuario ya verificado. */
    public boolean actualizarPassword(String username, String nuevaContrasena) throws SQLException {
        String sql = "UPDATE UsuarioSistema SET PasswordHash = ? WHERE Username = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevaContrasena);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    private UsuarioSistema mapear(ResultSet rs) throws SQLException {
        UsuarioSistema u = new UsuarioSistema();
        u.setUsuarioId(rs.getInt("UsuarioId"));
        u.setUsername(rs.getString("Username"));
        u.setNombres(rs.getString("Nombres"));
        u.setEmail(rs.getString("Email"));
        u.setRolId(rs.getInt("RolId"));
        u.setNombreRol(rs.getString("NombreRol"));
        int centro = rs.getInt("CentroComercialId");
        u.setCentroComercialId(rs.wasNull() ? null : centro);
        return u;
    }
}
