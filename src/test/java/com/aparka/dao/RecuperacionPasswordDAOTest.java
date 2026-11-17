package com.aparka.dao;

import com.aparka.util.ConexionBD;
import com.aparka.util.PasswordUtils;
import com.aparka.util.TokenRecuperacionUtil;

import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecuperacionPasswordDAOTest {

    private Connection conectarSoloAPruebas() throws SQLException {
        String url = System.getenv("APARKA_DB_URL");
        String usuario = System.getenv("APARKA_DB_USER");

        assertNotNull(url);
        assertTrue(
            url.matches("(?i)^jdbc:mysql://(?:localhost|127\\.0\\.0\\.1):3307/parking_test(?:\\?.*)?$"),
            "Solo se permite parking_test"
        );
        assertEquals("aparka_test", usuario);

        Connection con = ConexionBD.obtenerConexion();

        try {
            assertEquals("parking_test", con.getCatalog());
            return con;
        } catch (RuntimeException | SQLException e) {
            con.close();
            throw e;
        }
    }

    @Test
    void debeRegistrarTokenConExpiracionDe15Minutos() throws Exception {

        try (Connection con = conectarSoloAPruebas()) {

            int usuarioId = 0;
            String username = "test_" + UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12);

            try {
                String sqlUsuario =
                        "INSERT INTO usuariosistema "
                        + "(Username, PasswordHash, Nombres, Email, RolId, Activo) "
                        + "VALUES (?, ?, 'Usuario Temporal', ?, 2, 1)";

                try (PreparedStatement ps = con.prepareStatement(
                        sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, PasswordUtils.encriptar("PruebaInicial123!"));
                    ps.setString(3, username + "@example.invalid");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        usuarioId = rs.getInt(1);
                    }
                }

                String token = TokenRecuperacionUtil.generarToken();
                String hash = TokenRecuperacionUtil.calcularHash(token);

                RecuperacionPasswordDAO dao = new RecuperacionPasswordDAO();

                assertTrue(dao.registrarToken(usuarioId, hash));

                String sqlVerificar =
                        "SELECT TIMESTAMPDIFF(SECOND, NOW(), FechaExpiracion) "
                        + "AS SegundosRestantes "
                        + "FROM recuperacion_password "
                        + "WHERE UsuarioId = ? AND TokenHash = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                    ps.setInt(1, usuarioId);
                    ps.setString(2, hash);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());

                        int segundos = rs.getInt("SegundosRestantes");

                        assertTrue(
                            segundos >= 890 && segundos <= 900,
                            "La expiracion debe ser aproximadamente 15 minutos"
                        );
                    }
                }

            } finally {
                if (usuarioId > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM usuariosistema WHERE UsuarioId = ?")) {
                        ps.setInt(1, usuarioId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    @Test
    void debeCambiarPasswordEInvalidarToken() throws Exception {

        try (Connection con = conectarSoloAPruebas()) {

            int usuarioId = 0;
            String username = "test_" + UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12);

            String passwordAnterior = "PruebaInicial123!";
            String passwordNueva = "PruebaNueva456!";

            try {
                String sqlUsuario =
                        "INSERT INTO usuariosistema "
                        + "(Username, PasswordHash, Nombres, Email, RolId, Activo) "
                        + "VALUES (?, ?, 'Usuario Temporal', ?, 2, 1)";

                try (PreparedStatement ps = con.prepareStatement(
                        sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, PasswordUtils.encriptar(passwordAnterior));
                    ps.setString(3, username + "@example.invalid");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        usuarioId = rs.getInt(1);
                    }
                }

                String token = TokenRecuperacionUtil.generarToken();
                String hash = TokenRecuperacionUtil.calcularHash(token);

                RecuperacionPasswordDAO dao = new RecuperacionPasswordDAO();

                assertTrue(dao.registrarToken(usuarioId, hash));

                assertTrue(
                    dao.consumirTokenYCambiarPassword(token, passwordNueva),
                    "El primer uso del token debe permitir cambiar la contraseÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±a"
                );

                String sqlPassword =
                        "SELECT PasswordHash FROM usuariosistema "
                        + "WHERE UsuarioId = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlPassword)) {
                    ps.setInt(1, usuarioId);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());

                        String passwordHash = rs.getString("PasswordHash");

                        assertFalse(
                            PasswordUtils.verificar(passwordAnterior, passwordHash),
                            "La contraseÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±a anterior ya no debe funcionar"
                        );

                        assertTrue(
                            PasswordUtils.verificar(passwordNueva, passwordHash),
                            "La nueva contraseÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±a debe funcionar"
                        );
                    }
                }

                String sqlToken =
                        "SELECT FechaUso FROM recuperacion_password "
                        + "WHERE UsuarioId = ? AND TokenHash = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlToken)) {
                    ps.setInt(1, usuarioId);
                    ps.setString(2, hash);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());
                        assertNotNull(
                            rs.getTimestamp("FechaUso"),
                            "El token debe quedar marcado como utilizado"
                        );
                    }
                }

                assertFalse(
                    dao.consumirTokenYCambiarPassword(token, "OtraPassword789!"),
                    "El token no debe poder utilizarse por segunda vez"
                );

            } finally {
                if (usuarioId > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM usuariosistema WHERE UsuarioId = ?")) {
                        ps.setInt(1, usuarioId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    @Test
    void debeRechazarTokenVencido() throws Exception {

        try (Connection con = conectarSoloAPruebas()) {

            int usuarioId = 0;
            String username = "test_" + UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12);

            String passwordOriginal = "PruebaInicial123!";

            try {
                String sqlUsuario =
                        "INSERT INTO usuariosistema "
                        + "(Username, PasswordHash, Nombres, Email, RolId, Activo) "
                        + "VALUES (?, ?, 'Usuario Temporal', ?, 2, 1)";

                try (PreparedStatement ps = con.prepareStatement(
                        sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, PasswordUtils.encriptar(passwordOriginal));
                    ps.setString(3, username + "@example.invalid");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        usuarioId = rs.getInt(1);
                    }
                }

                String token = TokenRecuperacionUtil.generarToken();
                String hash = TokenRecuperacionUtil.calcularHash(token);

                RecuperacionPasswordDAO dao = new RecuperacionPasswordDAO();

                assertTrue(dao.registrarToken(usuarioId, hash));

                String sqlVencer =
                        "UPDATE recuperacion_password "
                        + "SET FechaExpiracion = DATE_SUB(NOW(), INTERVAL 1 MINUTE) "
                        + "WHERE UsuarioId = ? AND TokenHash = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlVencer)) {
                    ps.setInt(1, usuarioId);
                    ps.setString(2, hash);
                    assertEquals(1, ps.executeUpdate());
                }

                assertFalse(
                    dao.consumirTokenYCambiarPassword(
                        token, "PasswordNoPermitida456!"
                    ),
                    "Un token vencido debe ser rechazado"
                );

                String sqlPassword =
                        "SELECT PasswordHash FROM usuariosistema "
                        + "WHERE UsuarioId = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlPassword)) {
                    ps.setInt(1, usuarioId);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());

                        String passwordHash = rs.getString("PasswordHash");

                        assertTrue(
                            PasswordUtils.verificar(
                                passwordOriginal, passwordHash
                            ),
                            "La contraseÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â±a original debe permanecer vÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lida"
                        );
                    }
                }

            } finally {
                if (usuarioId > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM usuariosistema WHERE UsuarioId = ?")) {
                        ps.setInt(1, usuarioId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    @Test
    void debeRechazarTokenInvalido() throws Exception {

        try (Connection con = conectarSoloAPruebas()) {

            int usuarioId = 0;
            String username = "test_" + UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12);

            String passwordOriginal = "PruebaInicial123!";

            try {
                String sqlUsuario =
                        "INSERT INTO usuariosistema "
                        + "(Username, PasswordHash, Nombres, Email, RolId, Activo) "
                        + "VALUES (?, ?, 'Usuario Temporal', ?, 2, 1)";

                try (PreparedStatement ps = con.prepareStatement(
                        sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, PasswordUtils.encriptar(passwordOriginal));
                    ps.setString(3, username + "@example.invalid");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        usuarioId = rs.getInt(1);
                    }
                }

                String tokenNoRegistrado =
                        TokenRecuperacionUtil.generarToken();

                RecuperacionPasswordDAO dao = new RecuperacionPasswordDAO();

                assertFalse(
                    dao.consumirTokenYCambiarPassword(
                        tokenNoRegistrado, "PasswordNoPermitida456!"
                    ),
                    "Un token no registrado debe ser rechazado"
                );

                String sqlPassword =
                        "SELECT PasswordHash FROM usuariosistema "
                        + "WHERE UsuarioId = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlPassword)) {
                    ps.setInt(1, usuarioId);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());

                        String passwordHash = rs.getString("PasswordHash");

                        assertTrue(
                            PasswordUtils.verificar(
                                passwordOriginal, passwordHash
                            ),
                            "La contraseÃƒÆ’Ã‚Â±a original debe permanecer vÃƒÆ’Ã‚Â¡lida"
                        );
                    }
                }

            } finally {
                if (usuarioId > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM usuariosistema WHERE UsuarioId = ?")) {
                        ps.setInt(1, usuarioId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    @Test
    void debeRechazarPasswordCortaSinConsumirToken() throws Exception {

        try (Connection con = conectarSoloAPruebas()) {

            int usuarioId = 0;
            String username = "test_" + UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12);

            String passwordOriginal = "PruebaInicial123!";

            try {
                String sqlUsuario =
                        "INSERT INTO usuariosistema "
                        + "(Username, PasswordHash, Nombres, Email, RolId, Activo) "
                        + "VALUES (?, ?, 'Usuario Temporal', ?, 2, 1)";

                try (PreparedStatement ps = con.prepareStatement(
                        sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, PasswordUtils.encriptar(passwordOriginal));
                    ps.setString(3, username + "@example.invalid");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        usuarioId = rs.getInt(1);
                    }
                }

                String token = TokenRecuperacionUtil.generarToken();
                String hash = TokenRecuperacionUtil.calcularHash(token);

                RecuperacionPasswordDAO dao = new RecuperacionPasswordDAO();

                assertTrue(dao.registrarToken(usuarioId, hash));

                assertFalse(
                    dao.consumirTokenYCambiarPassword(token, "1234567"),
                    "Una contraseÃƒÂ±a de 7 caracteres debe ser rechazada"
                );

                String sqlVerificar =
                        "SELECT u.PasswordHash, r.FechaUso "
                        + "FROM usuariosistema u "
                        + "JOIN recuperacion_password r "
                        + "ON r.UsuarioId = u.UsuarioId "
                        + "WHERE u.UsuarioId = ? AND r.TokenHash = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                    ps.setInt(1, usuarioId);
                    ps.setString(2, hash);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());

                        assertTrue(
                            PasswordUtils.verificar(
                                passwordOriginal,
                                rs.getString("PasswordHash")
                            ),
                            "La contraseÃƒÂ±a original debe permanecer vÃƒÂ¡lida"
                        );

                        assertNull(
                            rs.getTimestamp("FechaUso"),
                            "El token no debe consumirse"
                        );
                    }
                }

            } finally {
                if (usuarioId > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM usuariosistema WHERE UsuarioId = ?")) {
                        ps.setInt(1, usuarioId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    @Test
    void debeRechazarTokenDeUsuarioInactivo() throws Exception {

        try (Connection con = conectarSoloAPruebas()) {

            int usuarioId = 0;
            String username = "test_" + UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12);

            String passwordOriginal = "PruebaInicial123!";

            try {
                String sqlUsuario =
                        "INSERT INTO usuariosistema "
                        + "(Username, PasswordHash, Nombres, Email, RolId, Activo) "
                        + "VALUES (?, ?, 'Usuario Temporal', ?, 2, 1)";

                try (PreparedStatement ps = con.prepareStatement(
                        sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, PasswordUtils.encriptar(passwordOriginal));
                    ps.setString(3, username + "@example.invalid");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        usuarioId = rs.getInt(1);
                    }
                }

                String token = TokenRecuperacionUtil.generarToken();
                String hash = TokenRecuperacionUtil.calcularHash(token);

                RecuperacionPasswordDAO dao = new RecuperacionPasswordDAO();

                assertTrue(dao.registrarToken(usuarioId, hash));

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuariosistema SET Activo = 0 WHERE UsuarioId = ?")) {
                    ps.setInt(1, usuarioId);
                    assertEquals(1, ps.executeUpdate());
                }

                assertFalse(
                    dao.consumirTokenYCambiarPassword(
                        token, "PasswordNoPermitida456!"
                    ),
                    "Un usuario inactivo no debe cambiar su contraseÃ±a"
                );

                String sqlVerificar =
                        "SELECT u.PasswordHash, r.FechaUso "
                        + "FROM usuariosistema u "
                        + "JOIN recuperacion_password r "
                        + "ON r.UsuarioId = u.UsuarioId "
                        + "WHERE u.UsuarioId = ? AND r.TokenHash = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                    ps.setInt(1, usuarioId);
                    ps.setString(2, hash);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());

                        assertTrue(
                            PasswordUtils.verificar(
                                passwordOriginal,
                                rs.getString("PasswordHash")
                            ),
                            "La contraseÃ±a original debe permanecer vÃ¡lida"
                        );

                        assertNull(
                            rs.getTimestamp("FechaUso"),
                            "El token no debe marcarse como utilizado"
                        );
                    }
                }

            } finally {
                if (usuarioId > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM usuariosistema WHERE UsuarioId = ?")) {
                        ps.setInt(1, usuarioId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    @Test
    void debePermitirSoloUnConsumoSimultaneo() throws Exception {

        try (Connection con = conectarSoloAPruebas()) {

            int usuarioId = 0;
            String username = "test_" + UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12);

            String passwordInicial = "PruebaInicial123!";
            String passwordA = "PasswordSimultaneaA123!";
            String passwordB = "PasswordSimultaneaB456!";

            java.util.concurrent.ExecutorService executor =
                    java.util.concurrent.Executors.newFixedThreadPool(2);

            try {
                String sqlUsuario =
                        "INSERT INTO usuariosistema "
                        + "(Username, PasswordHash, Nombres, Email, RolId, Activo) "
                        + "VALUES (?, ?, 'Usuario Temporal', ?, 2, 1)";

                try (PreparedStatement ps = con.prepareStatement(
                        sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, PasswordUtils.encriptar(passwordInicial));
                    ps.setString(3, username + "@example.invalid");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        usuarioId = rs.getInt(1);
                    }
                }

                String token = TokenRecuperacionUtil.generarToken();
                String hash = TokenRecuperacionUtil.calcularHash(token);

                RecuperacionPasswordDAO dao = new RecuperacionPasswordDAO();

                assertTrue(dao.registrarToken(usuarioId, hash));

                java.util.concurrent.CountDownLatch inicio =
                        new java.util.concurrent.CountDownLatch(1);

                java.util.concurrent.Future<Boolean> resultadoA =
                        executor.submit(() -> {
                            inicio.await();
                            return dao.consumirTokenYCambiarPassword(
                                    token, passwordA);
                        });

                java.util.concurrent.Future<Boolean> resultadoB =
                        executor.submit(() -> {
                            inicio.await();
                            return dao.consumirTokenYCambiarPassword(
                                    token, passwordB);
                        });

                inicio.countDown();

                boolean exitoA = resultadoA.get(
                        15, java.util.concurrent.TimeUnit.SECONDS);

                boolean exitoB = resultadoB.get(
                        15, java.util.concurrent.TimeUnit.SECONDS);

                assertNotEquals(
                    exitoA, exitoB,
                    "Solo una solicitud debe consumir el token"
                );

                String sqlVerificar =
                        "SELECT PasswordHash FROM usuariosistema "
                        + "WHERE UsuarioId = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                    ps.setInt(1, usuarioId);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());

                        String passwordHash = rs.getString("PasswordHash");

                        assertFalse(
                            PasswordUtils.verificar(passwordInicial, passwordHash),
                            "La contraseña inicial no debe funcionar"
                        );

                        assertTrue(
                            PasswordUtils.verificar(
                                exitoA ? passwordA : passwordB,
                                passwordHash
                            ),
                            "Debe conservarse la contraseña de la solicitud exitosa"
                        );

                        assertFalse(
                            PasswordUtils.verificar(
                                exitoA ? passwordB : passwordA,
                                passwordHash
                            ),
                            "La solicitud rechazada no debe cambiar la contraseña"
                        );
                    }
                }

            } finally {
                executor.shutdownNow();

                if (usuarioId > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM usuariosistema WHERE UsuarioId = ?")) {
                        ps.setInt(1, usuarioId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }
}