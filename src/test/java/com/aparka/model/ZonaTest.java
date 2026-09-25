package com.aparka.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias (TDD) del cálculo de flujo vehicular, que es la lógica
 * central de Aparka. Se prueba de forma aislada, sin necesidad de base de datos,
 * porque Zona.getNivelFlujo() y getPorcentajeOcupacion() son funciones puras.
 */
class ZonaTest {

    private Zona crearZona(int capacidadTotal, int capacidadOcupada) {
        Zona z = new Zona();
        z.setCapacidadTotal(capacidadTotal);
        z.setCapacidadOcupada(capacidadOcupada);
        return z;
    }

    @Test
    void porcentajeOcupacion_calculaCorrectamente() {
        Zona zona = crearZona(50, 25);
        assertEquals(50.0, zona.getPorcentajeOcupacion(), 0.001);
    }

    @Test
    void porcentajeOcupacion_conCapacidadCero_noLanzaExcepcion() {
        Zona zona = crearZona(0, 0);
        assertEquals(0.0, zona.getPorcentajeOcupacion());
    }

    @ParameterizedTest(name = "{0}/{1} plazas ocupadas → nivel {2}")
    @CsvSource({
            "10, 40, BAJO",   // 25%
            "39, 100, BAJO",  // 39% (justo debajo del umbral de MEDIO)
            "40, 100, MEDIO", // 40% exacto
            "79, 100, MEDIO", // 79%
            "80, 100, ALTO",  // 80% exacto
            "100, 100, ALTO"  // 100%
    })
    void nivelFlujo_seCalculaSegunLosUmbralesDefinidos(int ocupado, int total, String nivelEsperado) {
        Zona zona = crearZona(total, ocupado);
        assertEquals(nivelEsperado, zona.getNivelFlujo());
    }

    @Test
    void nivelFlujo_zonaVacia_esBajo() {
        Zona zona = crearZona(50, 0);
        assertEquals("BAJO", zona.getNivelFlujo());
    }

    @Test
    void nivelFlujo_zonaLlena_esAlto() {
        Zona zona = crearZona(50, 50);
        assertEquals("ALTO", zona.getNivelFlujo());
    }
}
