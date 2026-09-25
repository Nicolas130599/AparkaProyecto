package com.aparka.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para obtener la conexión a la base de datos.
 * Ajusta URL, usuario y contraseña según tu entorno.
 */
public class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3307/Parking?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "123456";
    // Recuerda ajustar el puerto (3306/3307) y la contraseña según tu configuración local.

    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de MySQL", e);
        }
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
