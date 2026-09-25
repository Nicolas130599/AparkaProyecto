package com.aparka.model;

public class UsuarioSistema {
    private int usuarioId;
    private String username;
    private String passwordHash;
    private String nombres;
    private String email;
    private int rolId;
    private String nombreRol; // se llena al hacer el JOIN con Rol
    private Integer centroComercialId;

    public UsuarioSistema() {}

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public Integer getCentroComercialId() { return centroComercialId; }
    public void setCentroComercialId(Integer centroComercialId) { this.centroComercialId = centroComercialId; }
}
