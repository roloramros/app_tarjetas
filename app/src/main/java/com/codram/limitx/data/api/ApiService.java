package com.codram.limitx.data.api;

import java.util.List; // Import List
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET; // Import GET
import retrofit2.http.Header;
import retrofit2.http.POST;

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

    @GET("/tarjetas")
    Call<List<TarjetaResponse>> getTarjetas(@Header("Authorization") String token);
}
