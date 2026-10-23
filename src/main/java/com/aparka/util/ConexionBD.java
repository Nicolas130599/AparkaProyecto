
package com.aparka.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona las conexiones MySQL de APARKA.
 *
 * La conexion debe configurarse mediante variables de entorno:
 * APARKA_DB_URL, APARKA_DB_USER y APARKA_DB_PASSWORD.
 *
 * No se utilizan credenciales ni bases de datos predeterminadas.
 */
public class ConexionBD {

    private static final Logger log =
            LoggerFactory.getLogger(ConexionBD.class);

    public static Connection obtenerConexion() throws SQLException {

        String url = obtenerVariableObligatoria("APARKA_DB_URL");
        String usuario = obtenerVariableObligatoria("APARKA_DB_USER");
        String password = obtenerVariableObligatoria("APARKA_DB_PASSWORD");

        if (!url.startsWith("jdbc:mysql://")) {
            throw new SQLException(
                    "APARKA_DB_URL debe ser una URL JDBC de MySQL."
            );
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se encontro el driver de MySQL.", e
            );
        }

        try {
            return DriverManager.getConnection(url, usuario, password);
        } catch (SQLException e) {
            log.error(
                    "No se pudo establecer la conexion con MySQL. SQLState: {}",
                    e.getSQLState()
            );
            throw e;
        }
    }

    private static String obtenerVariableObligatoria(String nombre)
            throws SQLException {

        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {
            throw new SQLException(
                    "Falta configurar la variable de entorno obligatoria: "
                    + nombre
            );
        }

        return valor.trim();
    }
}
