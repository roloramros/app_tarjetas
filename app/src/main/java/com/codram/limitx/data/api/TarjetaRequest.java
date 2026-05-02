package com.codram.limitx.data.api;

public class TarjetaRequest {
    private String nombre;
    private String numero;
    private String banco;
    private String moneda;
    private double limite_mensual;
    private boolean activa;

    public TarjetaRequest(String nombre, String numero, String banco, String moneda, double limite_mensual, boolean activa) {
        this.nombre = nombre;
        this.numero = numero;
        this.banco = banco;
        this.moneda = moneda;
        this.limite_mensual = limite_mensual;
        this.activa = activa;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getNumero() { return numero; }
    public String getBanco() { return banco; }
    public String getMoneda() { return moneda; }
    public double getLimiteMensual() { return limite_mensual; }
    public boolean isActiva() { return activa; }
}
