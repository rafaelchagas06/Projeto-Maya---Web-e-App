package com.example.rpgclinicapp.network;

import com.example.rpgclinicapp.models.AgendaRequest;
import com.example.rpgclinicapp.models.AgendaResponse;
import com.example.rpgclinicapp.models.CadastroRequest;
import com.example.rpgclinicapp.models.CheckinRequest;
import com.example.rpgclinicapp.models.Exercicio;
import com.example.rpgclinicapp.models.LoginRequest;
import com.example.rpgclinicapp.models.LoginResponse;
import com.example.rpgclinicapp.models.Prontuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ClinicApiService {

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @GET("/api/exercicios")
    Call<List<Exercicio>> getExercicios();

    @POST("/api/exercicios/checkin")
    Call<okhttp3.ResponseBody> salvarCheckin(@Body CheckinRequest request);
    @POST("/api/auth/register")
    Call<LoginResponse> cadastrar(@Body CadastroRequest request);

    @POST("/api/agendamentos/novo")
    Call<AgendaResponse> marcarSessao(@Body AgendaRequest request);

    @GET("api/prontuarios/{id}")
    Call<List<Prontuario>> getProntuarios(@Path("id") long pacienteId);
}
