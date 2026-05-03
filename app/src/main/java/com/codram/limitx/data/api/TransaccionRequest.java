package com.codram.limitx.data.api;

import java.util.UUID;
import java.math.BigDecimal;

public class TransaccionRequest {
    private UUID tarjeta_id;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private String subtipo;
    private boolean afecta_limite;
    private String fecha; // Formato ISO 8601

    public TransaccionRequest(UUID tarjeta_id, String tipo, BigDecimal monto, String descripcion, String subtipo, boolean afecta_limite, String fecha) {
        this.tarjeta_id = tarjeta_id;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.subtipo = subtipo;
        this.afecta_limite = afecta_limite;
        this.fecha = fecha;
    }
}
