package com.codram.limitx.data.api;

public class UsuarioResponse {
    private String id;
    private String nombre;
    private boolean suscripcion_activa;
    private String suscripcion_hasta;
    private String last_login;
    private String fecha_creacion;
    private int cantidad_tarjetas;
    private int cantidad_transacciones;

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public boolean isSuscripcionActiva() { return suscripcion_activa; }
    public String getSuscripcionHasta() { return suscripcion_hasta; }
    public String getLastLogin() { return last_login; }
    public String getFechaCreacion() { return fecha_creacion; }
    public int getCantidadTarjetas() { return cantidad_tarjetas; }
    public int getCantidadTransacciones() { return cantidad_transacciones; }
    public void setCantidadTransacciones(int cantidad_transacciones) { this.cantidad_transacciones = cantidad_transacciones; }
}
