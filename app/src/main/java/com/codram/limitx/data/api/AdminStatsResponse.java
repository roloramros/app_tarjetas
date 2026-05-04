package com.codram.limitx.data.api;

public class AdminStatsResponse {
    private int total_usuarios;
    private int total_tarjetas;
    private int total_transacciones;

    public int getTotal_usuarios() { return total_usuarios; }
    public void setTotal_usuarios(int total_usuarios) { this.total_usuarios = total_usuarios; }

    public int getTotal_tarjetas() { return total_tarjetas; }
    public void setTotal_tarjetas(int total_tarjetas) { this.total_tarjetas = total_tarjetas; }

    public int getTotal_transacciones() { return total_transacciones; }
    public void setTotal_transacciones(int total_transacciones) { this.total_transacciones = total_transacciones; }
}
