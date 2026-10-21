package com.aparka.model;

public class Zona {
    private int zonaId;
    private int centroComercialId;
    private String nombre;
    private String ubicacion;
    private int capacidadTotal;
    private int capacidadOcupada; // se calcula, no se guarda directo en la tabla Zona
    private double tarifaHora;
    private String nivel;   // plano al que pertenece: P1..P7 (pisos) o S1..S3 (sótanos); null si no tiene
    private Double planoX;  // posición sobre el plano, en % del ancho (0-100)
    private Double planoY;  // posición sobre el plano, en % del alto (0-100)

    public Zona() {}

    public double getPorcentajeOcupacion() {
        if (capacidadTotal == 0) return 0;
        return (capacidadOcupada * 100.0) / capacidadTotal;
    }

    public String getNivelFlujo() {
        double porcentaje = getPorcentajeOcupacion();
        if (porcentaje >= 80) return "ALTO";
        if (porcentaje >= 40) return "MEDIO";
        return "BAJO";
    }

    public int getZonaId() { return zonaId; }
    public void setZonaId(int zonaId) { this.zonaId = zonaId; }

    public int getCentroComercialId() { return centroComercialId; }
    public void setCentroComercialId(int centroComercialId) { this.centroComercialId = centroComercialId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public int getCapacidadTotal() { return capacidadTotal; }
    public void setCapacidadTotal(int capacidadTotal) { this.capacidadTotal = capacidadTotal; }

    public int getCapacidadOcupada() { return capacidadOcupada; }
    public void setCapacidadOcupada(int capacidadOcupada) { this.capacidadOcupada = capacidadOcupada; }

    public double getTarifaHora() { return tarifaHora; }
    public void setTarifaHora(double tarifaHora) { this.tarifaHora = tarifaHora; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public Double getPlanoX() { return planoX; }
    public void setPlanoX(Double planoX) { this.planoX = planoX; }

    public Double getPlanoY() { return planoY; }
    public void setPlanoY(Double planoY) { this.planoY = planoY; }

    /** true si la zona ya fue ubicada sobre un plano. */
    public boolean isTienePosicion() { return nivel != null && planoX != null && planoY != null; }
}
