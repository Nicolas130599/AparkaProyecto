package com.aparka.model;

public class Zona {
    private int id;
    private String nombre;
    private String ubicacion;
    private int capacidadTotal;
    private int capacidadOcupada;
    private double tarifaHora;

    public Zona() {}

    public Zona(int id, String nombre, String ubicacion, int capacidadTotal,
                int capacidadOcupada, double tarifaHora) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidadTotal = capacidadTotal;
        this.capacidadOcupada = capacidadOcupada;
        this.tarifaHora = tarifaHora;
    }

    /** Porcentaje de ocupación de la zona (0-100). */
    public double getPorcentajeOcupacion() {
        if (capacidadTotal == 0) return 0;
        return (capacidadOcupada * 100.0) / capacidadTotal;
    }

    /**
     * Nivel de flujo vehicular calculado en base al % de ocupación.
     * >= 80%  -> ALTO
     * >= 40%  -> MEDIO
     * <  40%  -> BAJO
     */
    public String getNivelFlujo() {
        double porcentaje = getPorcentajeOcupacion();
        if (porcentaje >= 80) return "ALTO";
        if (porcentaje >= 40) return "MEDIO";
        return "BAJO";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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
