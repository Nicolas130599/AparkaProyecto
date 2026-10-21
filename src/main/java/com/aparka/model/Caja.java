package com.aparka.model;

import java.util.List;

/**
 * Cajero / módulo de pago ubicado en un plano del estacionamiento (tabla CajaMaster).
 * Los campos pagos y monto se calculan para un rango de fechas a partir de la tabla Venta.
 */
public class Caja {
    private String cajaId;
    private String tipoCaja;
    private String ubicacion;
    private String nivel;   // P1..P7 / S1..S3; null si no está ubicado
    private Double planoX;  // % del ancho del plano
    private Double planoY;  // % del alto del plano
    private int pagos;      // ventas no anuladas en el rango
    private double monto;   // suma de MontoNeto en el rango
    private int nivelPagos; // intensidad 0-5 frente al cajero con más pagos (para el color en el plano)

    public String getCajaId() { return cajaId; }
    public void setCajaId(String cajaId) { this.cajaId = cajaId; }
    public String getTipoCaja() { return tipoCaja; }
    public void setTipoCaja(String tipoCaja) { this.tipoCaja = tipoCaja; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public Double getPlanoX() { return planoX; }
    public void setPlanoX(Double planoX) { this.planoX = planoX; }
    public Double getPlanoY() { return planoY; }
    public void setPlanoY(Double planoY) { this.planoY = planoY; }
    public int getPagos() { return pagos; }
    public void setPagos(int pagos) { this.pagos = pagos; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public int getNivelPagos() { return nivelPagos; }

    public boolean isTienePosicion() { return nivel != null && planoX != null && planoY != null; }

    public double getTicketPromedio() { return pagos == 0 ? 0 : monto / pagos; }

    /** Calcula la intensidad (0-5) de cada cajero comparando sus pagos con los del cajero más concurrido. */
    public static void calcularIntensidad(List<Caja> cajas) {
        int max = 0;
        for (Caja c : cajas) max = Math.max(max, c.pagos);
        for (Caja c : cajas) {
            c.nivelPagos = (c.pagos <= 0 || max <= 0) ? 0 : (int) Math.ceil(c.pagos * 5.0 / max);
        }
    }
}
