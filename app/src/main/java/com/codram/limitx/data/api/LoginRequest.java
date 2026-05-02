package com.codram.limitx.data.api;

public class LoginRequest {
    private String nombre;
    private String password;

    public LoginRequest(String nombre, String password) {
        this.nombre = nombre;
        this.password = password;
    }

    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
}
