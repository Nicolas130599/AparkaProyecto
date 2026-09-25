package com.aparka.model;

import java.sql.Timestamp;

/** Representa el registro de ingreso de un vehículo (tabla Entrada de la BD real). */
public class Entrada {
    private long parkingTransNo;
    private int centroComercialId;
    private String cardNo;
    private int puertaId;
    private Timestamp fechaHora;
    private Integer usuarioId;
    private Integer zonaId;
    private String placa;
    private int tieneSalida; // 0 = sigue estacionado, 1 = ya salió
    private Integer tipoClienteId;

    public Entrada() {}

    public long getParkingTransNo() { return parkingTransNo; }
    public void setParkingTransNo(long parkingTransNo) { this.parkingTransNo = parkingTransNo; }

    public int getCentroComercialId() { return centroComercialId; }
    public void setCentroComercialId(int centroComercialId) { this.centroComercialId = centroComercialId; }

    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public int getPuertaId() { return puertaId; }
    public void setPuertaId(int puertaId) { this.puertaId = puertaId; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public Integer getZonaId() { return zonaId; }
    public void setZonaId(Integer zonaId) { this.zonaId = zonaId; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public int getTieneSalida() { return tieneSalida; }
    public void setTieneSalida(int tieneSalida) { this.tieneSalida = tieneSalida; }

    public Integer getTipoClienteId() { return tipoClienteId; }
    public void setTipoClienteId(Integer tipoClienteId) { this.tipoClienteId = tipoClienteId; }
}
