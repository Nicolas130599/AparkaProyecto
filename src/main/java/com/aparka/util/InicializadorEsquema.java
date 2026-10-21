package com.aparka.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Al iniciar la aplicación revisa que existan las columnas que usan el perfil de usuario (foto) y las ubicaciones
 * sobre planos (zonas y cajeros), y agrega las que falten. Es seguro ejecutarlo en cada arranque: si ya existen,
 * no hace nada. Así no hace falta ejecutar scripts de migración a mano. Si algo falla solo se registra en el log
 * y la aplicación sigue iniciando.
 */
@WebListener
public class InicializadorEsquema implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(InicializadorEsquema.class);

    /** {tabla, columna, definición}: valores fijos del programa, nunca datos del usuario. */
    private static final String[][] COLUMNAS = {
            {"UsuarioSistema", "Documento", "VARCHAR(20) NULL"},
            {"UsuarioSistema", "Telefono", "VARCHAR(20) NULL"},
            {"UsuarioSistema", "FotoPerfil", "VARCHAR(100) NULL"},
            {"Zona", "Nivel", "VARCHAR(3) NULL"},
            {"Zona", "PlanoX", "DECIMAL(5,2) NULL"},
            {"Zona", "PlanoY", "DECIMAL(5,2) NULL"},
            {"CajaMaster", "Nivel", "VARCHAR(3) NULL"},
            {"CajaMaster", "PlanoX", "DECIMAL(5,2) NULL"},
            {"CajaMaster", "PlanoY", "DECIMAL(5,2) NULL"}
    };

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try (Connection con = ConexionBD.obtenerConexion()) {
            for (String[] c : COLUMNAS) {
                try {
                    if (!existeColumna(con, c[0], c[1])) {
                        try (Statement st = con.createStatement()) {
                            st.execute("ALTER TABLE " + c[0] + " ADD COLUMN " + c[1] + " " + c[2]);
                        }
                        log.info("Esquema actualizado: columna {}.{} creada", c[0], c[1]);
                    }
                } catch (SQLException e) {
                    log.warn("No se pudo verificar/crear la columna {}.{}: {}", c[0], c[1], e.getMessage());
                }
            }
        } catch (SQLException e) {
            log.error("No se pudo conectar a la base de datos para revisar el esquema: {}", e.getMessage());
        }
    }

    private boolean existeColumna(Connection con, String tabla, String columna) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tabla);
            ps.setString(2, columna);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
