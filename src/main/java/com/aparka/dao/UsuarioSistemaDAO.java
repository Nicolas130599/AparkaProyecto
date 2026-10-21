package com.aparka.dao;

import com.aparka.model.UsuarioSistema;
import com.aparka.util.ConexionBD;
import com.aparka.util.PasswordUtils;

import java.sql.*;

public class UsuarioSistemaDAO {

    /** Autentica contra UsuarioSistema verificando el hash de la contraseña con jBCrypt. */
    public UsuarioSistema autenticar(String username, String passwordPlana) throws SQLException {
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
                    if (passwordHashBD != null && PasswordUtils.verificar(passwordPlana, passwordHashBD)) {
                        return mapear(rs);
                    }
                }
            }
        }
        return null;
    }

    /** Registra un nuevo usuario encriptando su contraseña con jBCrypt. */
    public boolean registrar(UsuarioSistema u, String passwordPlana) throws SQLException {
        String passwordSegura = PasswordUtils.encriptar(passwordPlana);

        String sql = "INSERT INTO UsuarioSistema (Username, PasswordHash, Nombres, Email, Documento, Telefono, RolId, CentroComercialId) " +
                "VALUES (?, ?, ?, ?, ?, ?, 2, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, passwordSegura);
            ps.setString(3, u.getNombres());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getDocumento());
            ps.setString(6, u.getTelefono());
            ps.setInt(7, u.getCentroComercialId() != null ? u.getCentroComercialId() : 1);
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

    /** Busca un usuario activo por su nombre de usuario o su correo electrónico para la recuperación. */
    public UsuarioSistema buscarPorUsernameOEmail(String identificador) throws SQLException {
        String sql = "SELECT u.*, r.NombreRol FROM UsuarioSistema u " +
                     "JOIN Rol r ON u.RolId = r.RolId " +
                     "WHERE (u.Username = ? OR u.Email = ?) AND u.Activo = 1";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identificador);
            ps.setString(2, identificador);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

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

    public boolean actualizarPassword(String username, String nuevaContrasenaPlana) throws SQLException {
        String passwordSegura = PasswordUtils.encriptar(nuevaContrasenaPlana);

        String sql = "UPDATE UsuarioSistema SET PasswordHash = ? WHERE Username = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, passwordSegura);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }


    // ======================= PERFIL DE USUARIO =======================

    /** Actualiza los datos editables del perfil (el usuario, documento y rol no se tocan). */
    public boolean actualizarPerfil(int usuarioId, String nombres, String email, String telefono) throws SQLException {
        String sql = "UPDATE UsuarioSistema SET Nombres = ?, Email = ?, Telefono = ? WHERE UsuarioId = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombres);
            ps.setString(2, email);
            ps.setString(3, telefono);
            ps.setInt(4, usuarioId);
            return ps.executeUpdate() > 0;
        }
    }

    /** true si OTRO usuario (distinto de idExcluido) ya usa ese correo. */
    public boolean emailEnUsoPorOtro(String email, int idExcluido) throws SQLException {
        String sql = "SELECT UsuarioId FROM UsuarioSistema WHERE Email = ? AND UsuarioId <> ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Guarda el nombre del archivo de la foto de perfil; null la quita. */
    public boolean actualizarFoto(int usuarioId, String archivoFoto) throws SQLException {
        String sql = "UPDATE UsuarioSistema SET FotoPerfil = ? WHERE UsuarioId = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, archivoFoto);
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() > 0;
        }
    }

    private UsuarioSistema mapear(ResultSet rs) throws SQLException {
        UsuarioSistema u = new UsuarioSistema();
        u.setUsuarioId(rs.getInt("UsuarioId"));
        u.setUsername(rs.getString("Username"));
        u.setNombres(rs.getString("Nombres"));
        u.setEmail(rs.getString("Email"));
        u.setDocumento(rs.getString("Documento"));
        u.setTelefono(rs.getString("Telefono"));
        u.setRolId(rs.getInt("RolId"));
        u.setNombreRol(rs.getString("NombreRol"));
        int centro = rs.getInt("CentroComercialId");
        u.setCentroComercialId(rs.wasNull() ? null : centro);
        u.setFotoPerfil(leerFoto(rs));
        return u;
    }

    /** Lee FotoPerfil; si la columna aún no existe (aún no la creó InicializadorEsquema) no rompe el login. */
    private static String leerFoto(ResultSet rs) {
        try {
            return rs.getString("FotoPerfil");
        } catch (SQLException e) {
            return null;
        }
    }
}
