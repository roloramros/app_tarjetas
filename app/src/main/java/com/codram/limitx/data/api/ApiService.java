package com.codram.limitx.data.api;

import java.util.List;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/login")
    Call<TokenResponse> login(@Body LoginRequest request);

    @POST("/usuarios")
    Call<UsuarioResponse> register(@Body UsuarioCreate request);

    @POST("/tarjetas")
    Call<TarjetaResponse> createTarjeta(
        @Header("Authorization") String token,
        @Body TarjetaRequest request
    );

    @DELETE("/tarjetas/{tarjeta_id}")
    Call<Void> eliminarTarjeta(
        @Header("Authorization") String token,
        @Path("tarjeta_id") UUID tarjeta_id
    );

    @POST("/transacciones")
    Call<Void> crearTransaccion(
        @Header("Authorization") String token,
        @Body TransaccionRequest request
    );

    @GET("/tarjetas/{tarjeta_id}/transacciones/mes")
    Call<List<TransaccionResponse>> getTransaccionesMes(
        @Header("Authorization") String token,
        @Path("tarjeta_id") UUID tarjeta_id
    );

    @GET("/tarjetas")
    Call<List<TarjetaResponse>> getTarjetas(@Header("Authorization") String token);

    @GET("/usuarios")
    Call<List<UsuarioResponse>> getUsuarios(@Header("Authorization") String token);

    @DELETE("/usuarios/{user_id}")
    Call<Void> eliminarUsuario(
        @Header("Authorization") String token,
        @Path("user_id") String user_id
    );

    @GET("/usuarios/me")
    Call<UsuarioResponse> getMe(@Header("Authorization") String token);

    @GET("/admin/stats")
    Call<AdminStatsResponse> getAdminStats(@Header("Authorization") String token);

    @GET("/app-version")
    Call<AppVersionResponse> getAppVersion();
}
