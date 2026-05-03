package com.codram.limitx.data.api;

import java.util.UUID;

public class TarjetaResponse {
    private UUID id;
    private UUID usuario_id;
    private String nombre;
    private String numero;
    private String banco;
    private String moneda;
    private double limite_mensual;
    private boolean activa;
    private double saldo_tarjeta;
    private double extraccion_disponible;
    private double deposito_disponible;

    // Getters
    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuario_id; }
    public String getNombre() { return nombre; }
    public String getNumero() { return numero; }
    public String getBanco() { return banco; }
    public String getMoneda() { return moneda; }
    public double getLimiteMensual() { return limite_mensual; }
    public boolean isActiva() { return activa; }
    public double getSaldo_tarjeta() { return saldo_tarjeta; }
    public double getExtraccion_disponible() { return extraccion_disponible; }
    public double getDeposito_disponible() { return deposito_disponible; }
}
