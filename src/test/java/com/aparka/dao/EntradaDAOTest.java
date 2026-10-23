
package com.aparka.dao;

import com.aparka.model.Entrada;
import com.aparka.util.ConexionBD;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class EntradaDAOTest {

    private Connection conectarSoloAPruebas() throws SQLException {
        String url = System.getenv("APARKA_DB_URL");
        String usuario = System.getenv("APARKA_DB_USER");
        String password = System.getenv("APARKA_DB_PASSWORD");

        assertNotNull(url, "Falta APARKA_DB_URL");
        assertTrue(
            url.matches("(?i)^jdbc:mysql://(?:localhost|127\\.0\\.0\\.1):3307/parking_test(?:\\?.*)?$"),
            "La URL debe apuntar exclusivamente a parking_test"
        );
        assertEquals("aparka_test", usuario);
        assertNotNull(password, "Falta APARKA_DB_PASSWORD");
        assertFalse(password.isBlank(), "La contraseña está vacía");

        Connection con = ConexionBD.obtenerConexion();

        try {
            assertEquals(
                "parking_test",
                con.getCatalog(),
                "La conexión debe utilizar exclusivamente parking_test"
            );
            return con;
        } catch (RuntimeException | SQLException ex) {
            con.close();
            throw ex;
        }
    }

    private int crearZonaTemporal(Connection con) throws SQLException {
        String sql =
            "INSERT INTO Zona " +
            "(CentroComercialId, Nombre, Ubicacion, CapacidadTotal, TarifaHora, Nivel) " +
            "VALUES (1, ?, ?, 2, 0.00, 'TST')";

        try (PreparedStatement ps =
                 con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String identificador = UUID.randomUUID()
                .toString().substring(0, 8);

            ps.setString(1, "Zona Test " + identificador);
            ps.setString(2, "Temporal automatizada");

            assertEquals(1, ps.executeUpdate());

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next(), "No se generó el ID de la zona temporal");
                return rs.getInt(1);
            }
        }
    }

    private Entrada crearEntrada(
            String placa, String tarjeta, int zonaId) {

        Entrada entrada = new Entrada();
        entrada.setCentroComercialId(1);
        entrada.setPuertaId(1);
        entrada.setZonaId(zonaId);
        entrada.setCardNo(tarjeta);
        entrada.setPlaca(placa);
        return entrada;
    }

    private int contarOcupados(Connection con, int zonaId)
            throws SQLException {

        String sql =
            "SELECT COUNT(*) FROM Entrada " +
            "WHERE ZonaId = ? AND TieneSalida = 0";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, zonaId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private void limpiarEntradas(
            Connection con, int zonaId, String... tarjetas)
            throws SQLException {

        String sql =
            "DELETE FROM Entrada WHERE ZonaId = ? AND CardNo = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String tarjeta : tarjetas) {
                ps.setInt(1, zonaId);
                ps.setString(2, tarjeta);
                ps.addBatch();
            }

            if (tarjetas.length > 0) {
                ps.executeBatch();
            }
        }
    }

    private void eliminarZonaTemporal(Connection con, int zonaId)
            throws SQLException {

        try (PreparedStatement ps =
                 con.prepareStatement("DELETE FROM Zona WHERE ZonaId = ?")) {
            ps.setInt(1, zonaId);
            ps.executeUpdate();
        }
    }

    private String token() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 5)
            .toUpperCase(Locale.ROOT);
    }

    @Test
    void conexionDebeUtilizarExclusivamenteParkingTest()
            throws Exception {

        try (Connection con = conectarSoloAPruebas();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT DATABASE()")) {

            assertTrue(rs.next());
            assertEquals("parking_test", rs.getString(1));
        }
    }

    @Test
    void zonaLlena_debeRechazarTercerVehiculo()
            throws Exception {

        try (Connection con = conectarSoloAPruebas()) {
            int zonaId = crearZonaTemporal(con);
            String token = token();

            String tarjeta1 = "TST" + token + "01";
            String tarjeta2 = "TST" + token + "02";
            String tarjeta3 = "TST" + token + "03";

            try {
                EntradaDAO dao = new EntradaDAO();

                assertTrue(dao.registrarIngreso(
                    crearEntrada("T" + token + "1", tarjeta1, zonaId)
                ) > 0);

                assertTrue(dao.registrarIngreso(
                    crearEntrada("T" + token + "2", tarjeta2, zonaId)
                ) > 0);

                assertThrows(
                    SQLException.class,
                    () -> dao.registrarIngreso(
                        crearEntrada("T" + token + "3", tarjeta3, zonaId)
                    )
                );

                assertEquals(2, contarOcupados(con, zonaId));

            } finally {
                limpiarEntradas(
                    con, zonaId, tarjeta1, tarjeta2, tarjeta3
                );
                eliminarZonaTemporal(con, zonaId);
            }
        }
    }

    @Test
    void ingresosSimultaneos_noDebenSuperarCapacidad()
            throws Exception {

        try (Connection con = conectarSoloAPruebas()) {
            int zonaId = crearZonaTemporal(con);
            String token = token();

            String tarjetaInicial = "CON" + token + "01";
            String tarjeta2 = "CON" + token + "02";
            String tarjeta3 = "CON" + token + "03";

            ExecutorService executor =
                Executors.newFixedThreadPool(2);

            CountDownLatch preparados = new CountDownLatch(2);
            CountDownLatch iniciar = new CountDownLatch(1);

            Future<Boolean> futuro1 = null;
            Future<Boolean> futuro2 = null;

            try {
                EntradaDAO dao = new EntradaDAO();

                assertTrue(dao.registrarIngreso(
                    crearEntrada("C" + token + "1", tarjetaInicial, zonaId)
                ) > 0);

                assertEquals(1, contarOcupados(con, zonaId));

                Callable<Boolean> ingreso1 = () -> {
                    preparados.countDown();
                    iniciar.await();

                    try {
                        return dao.registrarIngreso(
                            crearEntrada("C" + token + "2", tarjeta2, zonaId)
                        ) > 0;
                    } catch (SQLException ex) {
                        return false;
                    }
                };

                Callable<Boolean> ingreso2 = () -> {
                    preparados.countDown();
                    iniciar.await();

                    try {
                        return dao.registrarIngreso(
                            crearEntrada("C" + token + "3", tarjeta3, zonaId)
                        ) > 0;
                    } catch (SQLException ex) {
                        return false;
                    }
                };

                futuro1 = executor.submit(ingreso1);
                futuro2 = executor.submit(ingreso2);

                assertTrue(preparados.await(10, TimeUnit.SECONDS));
                iniciar.countDown();

                boolean resultado1 = futuro1.get(20, TimeUnit.SECONDS);
                boolean resultado2 = futuro2.get(20, TimeUnit.SECONDS);

                assertEquals(
                    1,
                    (resultado1 ? 1 : 0) + (resultado2 ? 1 : 0),
                    "Solo uno de los dos vehículos debe ocupar el último espacio"
                );

                assertEquals(2, contarOcupados(con, zonaId));

            } finally {
                iniciar.countDown();

                if (futuro1 != null) futuro1.cancel(true);
                if (futuro2 != null) futuro2.cancel(true);

                executor.shutdownNow();

                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                        "Los hilos no terminaron; no se eliminará la zona"
                    );
                }

                try {
                    limpiarEntradas(
                        con, zonaId,
                        tarjetaInicial, tarjeta2, tarjeta3
                    );
                } finally {
                    eliminarZonaTemporal(con, zonaId);
                }
            }
        }
    }
}
