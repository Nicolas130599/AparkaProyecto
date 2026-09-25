package com.aparka.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias (TDD) de ValidacionUtil, la clase que integra
 * Google Guava y Apache Commons para validar los formularios del sistema.
 */
class ValidacionUtilTest {

    // ---------- esCorreoValido (Apache Commons Validator) ----------

    @ParameterizedTest
    @ValueSource(strings = {"admin@aparka.com", "usuario.demo@gmail.com", "a@b.co"})
    void esCorreoValido_conFormatosCorrectos_devuelveTrue(String correo) {
        assertTrue(ValidacionUtil.esCorreoValido(correo));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sin-arroba.com", "@sin-usuario.com", "espacios en@medio.com", "doble@@arroba.com"})
    void esCorreoValido_conFormatosIncorrectos_devuelveFalse(String correo) {
        assertFalse(ValidacionUtil.esCorreoValido(correo));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void esCorreoValido_conNuloOVacio_devuelveFalse(String correo) {
        assertFalse(ValidacionUtil.esCorreoValido(correo));
    }

    // ---------- requerirTextoNoVacio (Google Guava Preconditions) ----------

    @Test
    void requerirTextoNoVacio_conTextoValido_noLanzaExcepcion() {
        assertDoesNotThrow(() -> ValidacionUtil.requerirTextoNoVacio("Aparka", "Nombre"));
    }

    @ParameterizedTest
    @CsvSource({"'', Usuario", "'   ', Correo"})
    void requerirTextoNoVacio_conTextoVacioOBlanco_lanzaExcepcion(String valor, String campo) {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionUtil.requerirTextoNoVacio(valor, campo));
    }

    // ---------- esPlacaValida (Apache Commons Lang3) ----------

    @ParameterizedTest
    @ValueSource(strings = {"ABC-123", "XYZ-999", "A1B2C3"})
    void esPlacaValida_conPlacasDeLargoRazonable_devuelveTrue(String placa) {
        assertTrue(ValidacionUtil.esPlacaValida(placa));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB", "A"})
    void esPlacaValida_conPlacasMuyCortas_devuelveFalse(String placa) {
        assertFalse(ValidacionUtil.esPlacaValida(placa));
    }

    // ---------- esPasswordValida ----------

    @Test
    void esPasswordValida_conLargoSuficiente_devuelveTrue() {
        assertTrue(ValidacionUtil.esPasswordValida("demo123", 6));
    }

    @Test
    void esPasswordValida_conLargoInsuficiente_devuelveFalse() {
        assertFalse(ValidacionUtil.esPasswordValida("123", 6));
    }

    @Test
    void esPasswordValida_conLargoMinimoInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionUtil.esPasswordValida("cualquiera", 0));
    }
}
