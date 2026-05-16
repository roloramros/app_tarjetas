package com.codram.limitx.data.api;

import java.math.BigDecimal;

public class TransaccionUpdate {
    private BigDecimal monto;
    private String descripcion;
    private String fecha; // Formato ISO 8601

    public TransaccionUpdate(BigDecimal monto, String descripcion, String fecha) {
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public BigDecimal getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public String getFecha() { return fecha; }
}
