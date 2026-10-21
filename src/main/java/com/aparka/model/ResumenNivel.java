package com.aparka.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resumen de un plano/nivel: visitas (ingresos a las zonas de ese nivel) y pagos (ventas de los cajeros de ese nivel).
 * Ojo: son dos medidas distintas; una persona puede estacionar en un nivel y pagar en un cajero de otro.
 */
public class ResumenNivel {
    private final String nivel;
    private int zonas;
    private int visitas;
    private int cajeros;
    private int pagos;
    private double monto;

    private ResumenNivel(String nivel) { this.nivel = nivel; }

    public String getNivel() { return nivel; }
    public int getZonas() { return zonas; }
    public int getVisitas() { return visitas; }
    public int getCajeros() { return cajeros; }
    public int getPagos() { return pagos; }
    public double getMonto() { return monto; }

    /** Devuelve solo los niveles que tienen al menos una zona o un cajero ubicado, en el orden de {@code niveles}. */
    public static List<ResumenNivel> construir(List<String> niveles, List<Zona> zonas, MapaCalor mapa, List<Caja> cajas) {
        Map<String, MapaCalor.Fila> flujo = mapa.getPorZona();
        List<ResumenNivel> resultado = new ArrayList<>();
        for (String nivel : niveles) {
            ResumenNivel r = new ResumenNivel(nivel);
            for (Zona z : zonas) {
                if (nivel.equals(z.getNivel())) {
                    r.zonas++;
                    MapaCalor.Fila f = flujo.get(z.getNombre());
                    if (f != null) r.visitas += f.getTotal();
                }
            }
            for (Caja c : cajas) {
                if (nivel.equals(c.getNivel())) {
                    r.cajeros++;
                    r.pagos += c.getPagos();
                    r.monto += c.getMonto();
                }
            }
            if (r.zonas > 0 || r.cajeros > 0) resultado.add(r);
        }
        return resultado;
    }
}
