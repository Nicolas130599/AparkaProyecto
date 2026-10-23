package com.aparka.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para obtener la conexion a la base de datos.
 *
 * El puerto de MySQL cambia segun la instalacion (3307 o 3306), asi­ que se prueba 3307 y, si no responde,
 * 3306; el primero que funcione se recuerda para las siguientes conexiones. No hace falta editar nada.
 * Opcional: se puede forzar otra conexion con las variables de entorno APARKA_DB_URL, APARKA_DB_USER y
 * APARKA_DB_PASSWORD.
 */
public class ConexionBD {

    private static final Logger log = LoggerFactory.getLogger(ConexionBD.class);

    private static final String[] PUERTOS = {"3307", "3306"};
    private static final String BASE_Y_PARAMETROS = "/Parking?useSSL=false&connectionTimeZone=America/Lima&preserveInstants=false";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "";

    private static volatile String urlActiva; // la primera URL que conecto con exito

    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver de MySQL", e);
        }
        String usuario = variable("APARKA_DB_USER", USUARIO);
        String password = variable("APARKA_DB_PASSWORD", PASSWORD);

        String forzada = System.getenv("APARKA_DB_URL");
        if (forzada != null && !forzada.isBlank()) {
            return DriverManager.getConnection(forzada.trim(), usuario, password);
        }
        if (urlActiva != null) {
            return DriverManager.getConnection(urlActiva, usuario, password);
        }

        SQLException ultimo = null;
        for (String puerto : PUERTOS) {
            String url = "jdbc:mysql://localhost:" + puerto + BASE_Y_PARAMETROS;
            try {
                Connection con = DriverManager.getConnection(url, usuario, password);
                urlActiva = url;
                log.info("Conectado a MySQL en el puerto {}", puerto);
                return con;
            } catch (SQLException e) {
                log.warn("Sin conexion a MySQL en el puerto {}: {}", puerto, e.getMessage());
                if (ultimo != null) e.addSuppressed(ultimo);
                ultimo = e;
            }
        }
        throw ultimo;
    }

    private static String variable(String nombre, String porDefecto) {
        String v = System.getenv(nombre);
        return (v == null || v.isBlank()) ? porDefecto : v;
    }
}
