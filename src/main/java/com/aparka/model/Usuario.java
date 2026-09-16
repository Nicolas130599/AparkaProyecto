package com.aparka.model;

public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private String contrasena;
    private String placa;
    private String rol; // "USUARIO" o "ADMIN"

    public Usuario() {}

    public Usuario(int id, String nombre, String correo, String placa, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.placa = placa;
        this.rol = rol;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
