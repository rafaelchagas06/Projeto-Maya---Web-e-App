package com.example.rpgclinicapp.network;

import com.example.rpgclinicapp.models.AgendaRequest;
import com.example.rpgclinicapp.models.AgendaResponse;
import com.example.rpgclinicapp.models.CadastroRequest;
import com.example.rpgclinicapp.models.CheckinRequest;
import com.example.rpgclinicapp.models.Exercicio;
import com.example.rpgclinicapp.models.LoginRequest;
import com.example.rpgclinicapp.models.LoginResponse;
import com.example.rpgclinicapp.models.Notificacao;
import com.example.rpgclinicapp.models.Prontuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ClinicApiService {

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // --- MUDANÇA AQUI: A rota agora exige o ID do paciente na URL ---
    @GET("/api/exercicios/{paciente_id}")
    Call<List<Exercicio>> getExercicios(@Path("paciente_id") long pacienteId);

    @POST("/api/exercicios/checkin")
    Call<okhttp3.ResponseBody> salvarCheckin(@Body CheckinRequest request);

    @POST("/api/auth/register")
    Call<LoginResponse> cadastrar(@Body CadastroRequest request);

    @POST("/api/agendamentos/novo")
    Call<AgendaResponse> marcarSessao(@Body AgendaRequest request);

    @GET("api/prontuarios/{id}")
    Call<List<Prontuario>> getProntuarios(@Path("id") long pacienteId);

    // Busca as notificações filtrando pelo ID do paciente
    @GET("notificacoes?select=*")
    Call<List<Notificacao>> getNotificacoes(@Query("paciente_id") String pacienteIdEq);

    // Rota para marcar a notificação como lida (opcional, mas recomendado para o futuro)
    @PATCH("notificacoes")
    Call<Void> marcarComoLida(@Query("id") String idEq, @Body Notificacao notificacaoLida);
}