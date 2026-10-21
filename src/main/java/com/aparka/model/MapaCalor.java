package com.aparka.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo del mapa de calor de flujo vehicular: una cuadrícula de zonas (filas) x horas del día (24 columnas).
 * Calcula el "nivel" de color (0 = sin ingresos, 1..5 = de poco a mucho) de cada celda y los resúmenes
 * (zona con más flujo, hora pico). No depende de la base de datos, por eso es fácil de probar.
 */
public class MapaCalor {

    public static final int HORAS = 24;
    public static final int NIVELES = 5;

    /** Una fila de la cuadrícula: una zona con sus 24 contadores por hora. */
    public static class Fila {
        private final String zona;
        private final int[] horas;
        private final int[] niveles;
        private final int total;
        private final int nivelTotal;

        Fila(String zona, int[] horas, int[] niveles, int total, int nivelTotal) {
            this.zona = zona;
            this.horas = horas;
            this.niveles = niveles;
            this.total = total;
            this.nivelTotal = nivelTotal;
        }

        public String getZona() { return zona; }
        public int[] getHoras() { return horas; }
        public int[] getNiveles() { return niveles; }
        public int getTotal() { return total; }
        /** Intensidad global de la zona (0-5) comparada con la zona de mayor flujo: sirve para pintar el plano. */
        public int getNivelTotal() { return nivelTotal; }
    }

    private final List<Fila> filas;
    private final int[] totalesPorHora;
    private final int maximo;
    private final int totalGeneral;
    private final Fila zonaTop;
    private final int horaPico;

    private MapaCalor(List<Fila> filas, int[] totalesPorHora, int maximo, int totalGeneral,
                      Fila zonaTop, int horaPico) {
        this.filas = filas;
        this.totalesPorHora = totalesPorHora;
        this.maximo = maximo;
        this.totalGeneral = totalGeneral;
        this.zonaTop = zonaTop;
        this.horaPico = horaPico;
    }

    /** @param flujo nombre de zona -> 24 contadores (ingresos por hora, índice 0 = 00:00) */
    public static MapaCalor desde(Map<String, int[]> flujo) {
        int maximo = 0;
        for (int[] horas : flujo.values()) {
            for (int v : horas) maximo = Math.max(maximo, v);
        }

        int maxTotalZona = 0;
        for (int[] horas : flujo.values()) {
            int suma = 0;
            for (int v : horas) suma += Math.max(0, v);
            maxTotalZona = Math.max(maxTotalZona, suma);
        }

        List<Fila> filas = new ArrayList<>();
        int[] totalesPorHora = new int[HORAS];
        int totalGeneral = 0;
        Fila top = null;

        for (Map.Entry<String, int[]> e : flujo.entrySet()) {
            int[] horas = new int[HORAS];
            int[] niveles = new int[HORAS];
            int totalZona = 0;
            for (int h = 0; h < HORAS && h < e.getValue().length; h++) {
                int v = Math.max(0, e.getValue()[h]);
                horas[h] = v;
                niveles[h] = nivel(v, maximo);
                totalesPorHora[h] += v;
                totalZona += v;
            }
            Fila fila = new Fila(e.getKey(), horas, niveles, totalZona, nivel(totalZona, maxTotalZona));
            filas.add(fila);
            totalGeneral += totalZona;
            if (top == null || totalZona > top.total) top = fila;
        }

        int horaPico = -1;
        int mejor = 0;
        for (int h = 0; h < HORAS; h++) {
            if (totalesPorHora[h] > mejor) {
                mejor = totalesPorHora[h];
                horaPico = h;
            }
        }
        if (totalGeneral == 0) top = null;

        return new MapaCalor(Collections.unmodifiableList(filas), totalesPorHora, maximo,
                totalGeneral, top, horaPico);
    }

    /** 0 si no hay ingresos; de 1 (poco) a NIVELES (el máximo de la cuadrícula) en los demás casos. */
    static int nivel(int valor, int maximo) {
        if (valor <= 0 || maximo <= 0) return 0;
        return (int) Math.ceil(valor * (double) NIVELES / maximo);
    }

    public List<Fila> getFilas() { return filas; }

    /** Filas indexadas por nombre de zona (para cruzar con la lista de zonas en la pantalla de gestión). */
    public Map<String, Fila> getPorZona() {
        Map<String, Fila> mapa = new LinkedHashMap<>();
        for (Fila f : filas) mapa.put(f.zona, f);
        return mapa;
    }
    public int[] getTotalesPorHora() { return totalesPorHora; }
    public int getMaximo() { return maximo; }
    public int getTotalGeneral() { return totalGeneral; }
    public boolean isVacio() { return totalGeneral == 0; }

    /** Nombre de la zona con más ingresos en el rango (null si no hubo ingresos). */
    public String getZonaTop() { return zonaTop == null ? null : zonaTop.zona; }
    public int getTotalZonaTop() { return zonaTop == null ? 0 : zonaTop.total; }

    /** Hora (0-23) con más ingresos sumando todas las zonas; -1 si no hubo ingresos. */
    public int getHoraPico() { return horaPico; }
    public int getTotalHoraPico() { return horaPico < 0 ? 0 : totalesPorHora[horaPico]; }
}
