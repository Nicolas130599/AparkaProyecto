package com.aparka.model;

import java.sql.Timestamp;

/** Representa una fila de la consulta de flujo vehicular: puede ser una Entrada o una Salida. */
public class Movimiento {
    private long ticket;
    private String tipo; // "Entrada" o "Salida"
    private String placa;
    private Timestamp fechaHora;
    private String puerta;
    private String zona;

    public Movimiento() {}

    public long getTicket() { return ticket; }
    public void setTicket(long ticket) { this.ticket = ticket; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }

    public String getPuerta() { return puerta; }
    public void setPuerta(String puerta) { this.puerta = puerta; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
}
