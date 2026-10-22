
package com.aparka.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptLimiterTest {

    @Test
    void debeBloquearDespuesDeCincoIntentosFallidos() {
        String usuario = "prueba_bloqueo_01";
        String ip = "192.0.2.101";

        assertFalse(LoginAttemptLimiter.estaBloqueado(usuario, ip));

        for (int i = 1; i <= 4; i++) {
            LoginAttemptLimiter.registrarFallo(usuario, ip);

            assertFalse(
                    LoginAttemptLimiter.estaBloqueado(usuario, ip)
            );
        }

        LoginAttemptLimiter.registrarFallo(usuario, ip);

        assertTrue(
                LoginAttemptLimiter.estaBloqueado(usuario, ip)
        );
    }

    @Test
    void noDebeBloquearOtroUsuarioConPocosIntentos() {
        String ip = "192.0.2.102";
        String usuarioA = "prueba_usuario_A";
        String usuarioB = "prueba_usuario_B";

        for (int i = 0; i < 5; i++) {
            LoginAttemptLimiter.registrarFallo(usuarioA, ip);
        }

        assertTrue(
                LoginAttemptLimiter.estaBloqueado(usuarioA, ip)
        );

        assertFalse(
                LoginAttemptLimiter.estaBloqueado(usuarioB, ip)
        );
    }

    @Test
    void debeReiniciarContadorDespuesDeLoginExitoso() {
        String usuario = "prueba_exito_01";
        String ip = "192.0.2.103";

        for (int i = 0; i < 5; i++) {
            LoginAttemptLimiter.registrarFallo(usuario, ip);
        }

        assertTrue(
                LoginAttemptLimiter.estaBloqueado(usuario, ip)
        );

        LoginAttemptLimiter.registrarExito(usuario, ip);

        assertFalse(
                LoginAttemptLimiter.estaBloqueado(usuario, ip)
        );
    }

    @Test
    void debeBloquearCuandoUnaIpAcumulaTreintaFallos() {
        String ip = "192.0.2.104";

        for (int i = 0; i < 30; i++) {
            LoginAttemptLimiter.registrarFallo(
                    "usuario_diferente_" + i, ip
            );
        }

        assertTrue(
                LoginAttemptLimiter.estaBloqueado(
                        "usuario_nuevo", ip
                )
        );
    }

    @Test
    void debeMantenerBloqueoAnteIntentosAdicionales() {
        String usuario = "prueba_persistencia_01";
        String ip = "192.0.2.201";

        for (int i = 0; i < 5; i++) {
            LoginAttemptLimiter.registrarFallo(usuario, ip);
        }

        assertTrue(
                LoginAttemptLimiter.estaBloqueado(usuario, ip)
        );

        for (int i = 0; i < 10; i++) {
            LoginAttemptLimiter.registrarFallo(usuario, ip);
        }

        assertTrue(
                LoginAttemptLimiter.estaBloqueado(usuario, ip)
        );
    }

    @Test
    void debePermitirUsuarioDistintoEnOtraIp() {
        String usuarioA = "prueba_aislamiento_A";
        String ipA = "192.0.2.202";

        String usuarioB = "prueba_aislamiento_B";
        String ipB = "192.0.2.203";

        for (int i = 0; i < 5; i++) {
            LoginAttemptLimiter.registrarFallo(usuarioA, ipA);
        }

        assertTrue(
                LoginAttemptLimiter.estaBloqueado(usuarioA, ipA)
        );

        assertFalse(
                LoginAttemptLimiter.estaBloqueado(usuarioB, ipB)
        );
    }
}
