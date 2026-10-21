package com.aparka.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del cálculo de niveles de color y resúmenes del mapa de calor. */
class MapaCalorTest {

    private static int[] horas(int... pares) { // pares: hora, valor, hora, valor...
        int[] h = new int[24];
        for (int i = 0; i < pares.length; i += 2) h[pares[i]] = pares[i + 1];
        return h;
    }

    @Test
    void nivel_sinIngresos_esCero() {
        assertEquals(0, MapaCalor.nivel(0, 50));
        assertEquals(0, MapaCalor.nivel(5, 0));
    }

    @Test
    void nivel_elMaximoSiempreEsElNivelMasAlto() {
        assertEquals(MapaCalor.NIVELES, MapaCalor.nivel(50, 50));
    }

    @Test
    void nivel_valoresPequenosNuncaQuedanEnCero() {
        assertEquals(1, MapaCalor.nivel(1, 50));
        assertEquals(3, MapaCalor.nivel(26, 50));
    }

    @Test
    void desde_calculaZonaTopHoraPicoYTotales() {
        Map<String, int[]> flujo = new LinkedHashMap<>();
        flujo.put("Zona Centro", horas(12, 10, 18, 30));
        flujo.put("Zona Norte", horas(8, 4));
        flujo.put("Zona Universidad", horas(18, 20, 7, 40));

        MapaCalor mapa = MapaCalor.desde(flujo);

        assertEquals(3, mapa.getFilas().size());
        assertEquals(104, mapa.getTotalGeneral());
        assertEquals(40, mapa.getMaximo());
        assertEquals("Zona Universidad", mapa.getZonaTop());
        assertEquals(60, mapa.getTotalZonaTop());
        assertEquals(18, mapa.getHoraPico());
        assertEquals(50, mapa.getTotalHoraPico());
        assertEquals(MapaCalor.NIVELES, mapa.getFilas().get(2).getNiveles()[7]);
        assertEquals(0, mapa.getFilas().get(1).getNiveles()[0]);
    }

    @Test
    void desde_sinIngresos_quedaVacioSinResumenes() {
        Map<String, int[]> flujo = new LinkedHashMap<>();
        flujo.put("Zona Centro", new int[24]);

        MapaCalor mapa = MapaCalor.desde(flujo);

        assertTrue(mapa.isVacio());
        assertNull(mapa.getZonaTop());
        assertEquals(-1, mapa.getHoraPico());
    }

    @Test
    void desde_calculaIntensidadPorZonaYAccesoPorNombre() {
        Map<String, int[]> flujo = new LinkedHashMap<>();
        flujo.put("Zona A", horas(8, 100));          // la de mayor flujo => nivel 5
        flujo.put("Zona B", horas(8, 10, 9, 10));    // 20 de 100 => nivel 1
        flujo.put("Zona C", new int[24]);            // sin ingresos => nivel 0

        MapaCalor mapa = MapaCalor.desde(flujo);

        assertEquals(MapaCalor.NIVELES, mapa.getPorZona().get("Zona A").getNivelTotal());
        assertEquals(1, mapa.getPorZona().get("Zona B").getNivelTotal());
        assertEquals(0, mapa.getPorZona().get("Zona C").getNivelTotal());
        assertNull(mapa.getPorZona().get("Zona inexistente"));
    }
}
