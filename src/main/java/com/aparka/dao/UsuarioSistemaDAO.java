package com.aparka.dao;

import com.aparka.model.UsuarioSistema;
import com.aparka.util.ConexionBD;
import com.aparka.util.PasswordUtils;

import java.sql.*;

public class UsuarioSistemaDAO {

    /** Autentica contra UsuarioSistema verificando el hash de la contraseña con jBCrypt. */
    public UsuarioSistema autenticar(String username, String passwordPlana) throws SQLException {
        // Obtenemos al usuario por su username (sin comparar el password en el SQL)
        String sql = "SELECT u.*, r.NombreRol " +
                "FROM UsuarioSistema u " +
                "JOIN Rol r ON u.RolId = r.RolId " +
                "WHERE u.Username = ? AND u.Activo = 1";
                
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String passwordHashBD = rs.getString("PasswordHash");
                    // Verificamos si la contraseña plana coincide con el hash almacenado
                    if (passwordHashBD != null && PasswordUtils.verificar(passwordPlana, passwordHashBD)) {
                        return mapear(rs);
                    }
                }
            }
        }
        return null; // Credenciales inválidas
    }

    /** Registra un nuevo usuario encriptando su contraseña con jBCrypt. */
    public boolean registrar(UsuarioSistema u, String passwordPlana) throws SQLException {
        // Encriptamos la contraseña antes de guardarla para cumplir con la seguridad de la rúbrica
        String passwordSegura = PasswordUtils.encriptar(passwordPlana);

        String sql = "INSERT INTO UsuarioSistema (Username, PasswordHash, Nombres, Email, RolId, CentroComercialId) " +
                "VALUES (?, ?, ?, ?, 2, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, passwordSegura); // Guardamos el hash seguro
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

    /** Actualiza la contraseña de un usuario ya verificado, encriptando la nueva clave. */
    public boolean actualizarPassword(String username, String nuevaContrasenaPlana) throws SQLException {
        // Encriptamos la nueva contraseña
        String passwordSegura = PasswordUtils.encriptar(nuevaContrasenaPlana);

        String sql = "UPDATE UsuarioSistema SET PasswordHash = ? WHERE Username = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, passwordSegura);
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