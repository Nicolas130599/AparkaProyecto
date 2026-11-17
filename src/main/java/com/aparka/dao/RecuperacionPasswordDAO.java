
package com.aparka.dao;

import com.aparka.util.ConexionBD;
import com.aparka.util.PasswordUtils;
import com.aparka.util.TokenRecuperacionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecuperacionPasswordDAO {

    private static final int MINUTOS_EXPIRACION = 15;

    public boolean registrarToken(int usuarioId, String tokenHash)
            throws SQLException {

        if (usuarioId <= 0 || tokenHash == null
                || !tokenHash.matches("[0-9a-f]{64}")) {

            throw new IllegalArgumentException(
                    "Datos de token invalidos");
        }

        String sql = "INSERT INTO recuperacion_password "
                + "(UsuarioId, TokenHash, FechaCreacion, FechaExpiracion) "
                + "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE))";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, tokenHash);
            ps.setInt(3, MINUTOS_EXPIRACION);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean consumirTokenYCambiarPassword(
            String token, String nuevaPassword) throws SQLException {

        String tokenHash;

        try {
            tokenHash = TokenRecuperacionUtil.calcularHash(token);

        } catch (IllegalArgumentException e) {
            return false;
        }

        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            return false;
        }

        String passwordHash = PasswordUtils.encriptar(nuevaPassword);

        try (Connection con = ConexionBD.obtenerConexion()) {

            con.setAutoCommit(false);

            try {
                Integer usuarioId = null;

                String sqlToken =
                        "SELECT r.UsuarioId "
                        + "FROM recuperacion_password r "
                        + "JOIN UsuarioSistema u "
                        + "ON u.UsuarioId = r.UsuarioId "
                        + "WHERE r.TokenHash = ? "
                        + "AND r.FechaUso IS NULL "
                        + "AND r.FechaExpiracion > NOW() "
                        + "AND u.Activo = 1 "
                        + "FOR UPDATE";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlToken)) {

                    ps.setString(1, tokenHash);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            usuarioId = rs.getInt("UsuarioId");
                        }
                    }
                }

                if (usuarioId == null) {
                    con.rollback();
                    return false;
                }

                String sqlPassword =
                        "UPDATE UsuarioSistema "
                        + "SET PasswordHash = ? "
                        + "WHERE UsuarioId = ? "
                        + "AND Activo = 1";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlPassword)) {

                    ps.setString(1, passwordHash);
                    ps.setInt(2, usuarioId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }

                String sqlInvalidar =
                        "UPDATE recuperacion_password "
                        + "SET FechaUso = NOW() "
                        + "WHERE UsuarioId = ? "
                        + "AND FechaUso IS NULL";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlInvalidar)) {

                    ps.setInt(1, usuarioId);

                    int tokensInvalidados = ps.executeUpdate();

                    if (tokensInvalidados < 1) {
                        con.rollback();
                        return false;
                    }
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;
            }
        }
    }
}
