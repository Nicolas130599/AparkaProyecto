package com.aparka.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenRecuperacionUtilTest {

    @Test
    void debeGenerarTokenConFormatoCorrecto() {
        String token = TokenRecuperacionUtil.generarToken();

        assertNotNull(token);
        assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
    }

    @Test
    void debeGenerarTokensDiferentes() {
        String token1 = TokenRecuperacionUtil.generarToken();
        String token2 = TokenRecuperacionUtil.generarToken();

        assertNotEquals(token1, token2);
    }

    @Test
    void debeGenerarHashSHA256() {
        String token = TokenRecuperacionUtil.generarToken();
        String hash = TokenRecuperacionUtil.calcularHash(token);

        assertTrue(hash.matches("[0-9a-f]{64}"));
        assertNotEquals(token, hash);
    }

    @Test
    void mismoTokenDebeGenerarMismoHash() {
        String token = TokenRecuperacionUtil.generarToken();

        assertEquals(
                TokenRecuperacionUtil.calcularHash(token),
                TokenRecuperacionUtil.calcularHash(token)
        );
    }

    @Test
    void debeRechazarTokensInvalidos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TokenRecuperacionUtil.calcularHash(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> TokenRecuperacionUtil.calcularHash("token-invalido")
        );
    }
}