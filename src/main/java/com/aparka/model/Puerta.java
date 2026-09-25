package com.aparka.model;

public class Puerta {
    private int puertaId;
    private String descripcion;
    private String tipo;

    public Puerta() {}

    public int getPuertaId() { return puertaId; }
    public void setPuertaId(int puertaId) { this.puertaId = puertaId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
