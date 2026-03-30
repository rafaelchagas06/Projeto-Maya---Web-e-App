package com.example.rpgclinicapp.network;

import com.example.rpgclinicapp.models.AgendaRequest;
import com.example.rpgclinicapp.models.AgendaResponse;
import com.example.rpgclinicapp.models.CadastroRequest;
import com.example.rpgclinicapp.models.LoginRequest;
import com.example.rpgclinicapp.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ClinicApiService {

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/auth/register")
    Call<LoginResponse> cadastrar(@Body CadastroRequest request);

    @POST("/api/agendamentos/novo")
    Call<AgendaResponse> marcarSessao(@Body AgendaRequest request);

}
