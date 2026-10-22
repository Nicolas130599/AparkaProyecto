
package com.aparka.util;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class LoginAttemptLimiter {

    private static final int MAX_INTENTOS = 5;
    private static final int MAX_INTENTOS_IP = 30;
    private static final int MAX_REGISTROS = 10000;

    private static final long VENTANA =
            TimeUnit.MINUTES.toMillis(15);

    private static final ConcurrentHashMap<String, Registro> registros =
            new ConcurrentHashMap<>();

    private LoginAttemptLimiter() {
    }

    public static synchronized boolean estaBloqueado(
            String username, String ip) {

        long ahora = System.currentTimeMillis();
        limpiarVencidos(ahora);

        if (registros.size() >= MAX_REGISTROS) {
            return true;
        }

        return bloqueado(
                "USER:" + claveUsuario(username, ip),
                MAX_INTENTOS
        ) || bloqueado(
                "IP:" + normalizar(ip),
                MAX_INTENTOS_IP
        );
    }

    public static synchronized void registrarFallo(
            String username, String ip) {

        long ahora = System.currentTimeMillis();
        limpiarVencidos(ahora);

        if (registros.size() >= MAX_REGISTROS) {
            return;
        }

        registrar(
                "USER:" + claveUsuario(username, ip),
                ahora
        );

        if (registros.size() < MAX_REGISTROS
                || registros.containsKey("IP:" + normalizar(ip))) {
            registrar(
                    "IP:" + normalizar(ip),
                    ahora
            );
        }
    }

    public static synchronized void registrarExito(
            String username, String ip) {

        registros.remove(
                "USER:" + claveUsuario(username, ip)
        );
    }

    private static void limpiarVencidos(long ahora) {
        registros.entrySet().removeIf(
                entry -> ahora - entry.getValue().inicio >= VENTANA
        );
    }

    private static boolean bloqueado(
            String clave, int limite) {

        Registro registro = registros.get(clave);

        return registro != null
                && registro.intentos >= limite;
    }

    private static void registrar(String clave, long ahora) {

        registros.compute(clave, (k, anterior) -> {

            if (anterior == null
                    || ahora - anterior.inicio >= VENTANA) {

                return new Registro(ahora, 1);
            }

            return new Registro(
                    anterior.inicio,
                    anterior.intentos + 1
            );
        });
    }

    private static String claveUsuario(
            String username, String ip) {

        return normalizar(username)
                .toLowerCase(Locale.ROOT)
                + ":" + normalizar(ip);
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static final class Registro {

        private final long inicio;
        private final int intentos;

        private Registro(long inicio, int intentos) {
            this.inicio = inicio;
            this.intentos = intentos;
        }
    }
}
