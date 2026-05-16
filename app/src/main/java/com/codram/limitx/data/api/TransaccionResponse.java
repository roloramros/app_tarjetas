package com.codram.limitx.data.api;

import java.math.BigDecimal;
import java.util.UUID;

public class TransaccionResponse {
    private UUID id;
    private UUID tarjeta_id;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private String subtipo;
    private boolean afecta_limite;
    private String fecha;

    public UUID getId() { return id; }
    public UUID getTarjetaId() { return tarjeta_id; }
    public String getTipo() { return tipo; }
    public BigDecimal getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public String getSubtipo() { return subtipo; }
    public boolean isAfectaLimite() { return afecta_limite; }
    public String getFecha() { return fecha; }
}
