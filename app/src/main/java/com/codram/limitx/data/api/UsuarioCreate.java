package com.codram.limitx.data.api;

public class UsuarioCreate {
    private String nombre;
    private String password;

    public UsuarioCreate(String nombre, String password) {
        this.nombre = nombre;
        this.password = password;
    }

    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
}
