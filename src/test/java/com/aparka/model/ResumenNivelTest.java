package com.aparka.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del resumen por nivel (visitas por zonas vs. pagos por cajeros) y de la intensidad de los cajeros. */
class ResumenNivelTest {

    private static Zona zona(String nombre, String nivel) {
        Zona z = new Zona();
        z.setNombre(nombre);
        z.setNivel(nivel);
        return z;
    }

    private static Caja caja(String id, String nivel, int pagos, double monto) {
        Caja c = new Caja();
        c.setCajaId(id);
        c.setNivel(nivel);
        c.setPagos(pagos);
        c.setMonto(monto);
        return c;
    }

    @Test
    void construir_sumaVisitasPorZonasYPagosPorCajerosDeCadaNivel() {
        Map<String, int[]> flujo = new LinkedHashMap<>();
        int[] a = new int[24]; a[8] = 30; a[18] = 20;
        int[] b = new int[24]; b[9] = 10;
        flujo.put("Zona A", a);
        flujo.put("Zona B", b);
        MapaCalor mapa = MapaCalor.desde(flujo);

        List<Zona> zonas = List.of(zona("Zona A", "P1"), zona("Zona B", "P2"), zona("Zona sin nivel", null));
        List<Caja> cajas = List.of(caja("C1", "P1", 5, 50.0), caja("C2", "P1", 3, 30.5), caja("C3", "S1", 7, 70.0));

        List<ResumenNivel> r = ResumenNivel.construir(List.of("P1", "P2", "S1", "S2"), zonas, mapa, cajas);

        assertEquals(3, r.size(), "S2 no tiene zonas ni cajeros, no debe aparecer");
        assertEquals("P1", r.get(0).getNivel());
        assertEquals(50, r.get(0).getVisitas());
        assertEquals(8, r.get(0).getPagos());
        assertEquals(80.5, r.get(0).getMonto(), 0.001);
        assertEquals(2, r.get(0).getCajeros());
        assertEquals(10, r.get(1).getVisitas());
        assertEquals(0, r.get(1).getPagos());
        assertEquals(0, r.get(2).getVisitas(), "S1 solo tiene cajeros");
        assertEquals(7, r.get(2).getPagos());
    }

    @Test
    void calcularIntensidad_elCajeroConMasPagosEsNivel5() {
        List<Caja> cajas = new ArrayList<>(List.of(caja("A", "P1", 100, 0), caja("B", "P1", 20, 0), caja("C", "P1", 0, 0)));
        Caja.calcularIntensidad(cajas);
        assertEquals(5, cajas.get(0).getNivelPagos());
        assertEquals(1, cajas.get(1).getNivelPagos());
        assertEquals(0, cajas.get(2).getNivelPagos());
    }

    @Test
    void ticketPromedio_conYSinPagos() {
        assertEquals(10.0, caja("A", "P1", 4, 40.0).getTicketPromedio(), 0.001);
        assertEquals(0.0, caja("B", "P1", 0, 0).getTicketPromedio(), 0.001);
    }
}
