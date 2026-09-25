package com.aparka.model;

public class Zona {
    private int zonaId;
    private int centroComercialId;
    private String nombre;
    private String ubicacion;
    private int capacidadTotal;
    private int capacidadOcupada; // se calcula, no se guarda directo en la tabla Zona
    private double tarifaHora;

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
}
