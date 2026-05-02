package com.example.rpgclinicapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rpgclinicapp.models.AgendaRequest;
import com.example.rpgclinicapp.models.AgendaResponse;
import com.example.rpgclinicapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AgendaActivity extends AppCompatActivity {

    private String selectedDate = "";
    private String selectedTime = "";

    // Componentes Visuais
    private TextView tvTextoConfirmacao;
    private TextView tvUltimoSelecionado = null;
    private Button btnAgendar;

    // Lista para simular horários que já estão ocupados (futuro SQLite)
    private List<String> horariosOcupados = new ArrayList<>();

    // Array com todos os IDs dos horários do seu XML para facilitar o bloqueio
    private final int[] idsHorarios = {
            R.id.hora_0800, R.id.hora_0900, R.id.hora_1000,
            R.id.hora_1100, R.id.hora_1300, R.id.hora_1400,
            R.id.hora_1500, R.id.hora_1600, R.id.hora_1700
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Oculta a Action Bar superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_agenda);


        tvTextoConfirmacao = findViewById(R.id.tv_texto_confirmacao);
        btnAgendar = findViewById(R.id.btn_agendar);
        CalendarView calendarView = findViewById(R.id.calendarView);

        // Pinta de cinza os horários que já estão na lista de ocupados
        atualizarVisualHorarios();

        // Configuração do Calendário
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(new Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
        });

        // Configura Barra Inferior e Botão Voltar
        configurarNavegacao();

        // Clique do botão principal do Retrofit
        btnAgendar.setOnClickListener(v -> realizarAgendamento());
    }

    // --- MÉTODOS DE CONTROLE DOS HORÁRIOS ---

    // Este método é chamado direto do XML (android:onClick="selecionarHora")
    public void selecionarHora(View view) {
        TextView tvClicada = (TextView) view;
        String horaEscolhida = tvClicada.getText().toString();

        // 1. Verifica se está bloqueado
        if (horariosOcupados.contains(horaEscolhida)) {
            Toast.makeText(this, "Horário indisponível!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Tira a cor "Selecionada" do botão anterior que o usuário tinha clicado
        if (tvUltimoSelecionado != null) {
            tvUltimoSelecionado.setBackgroundResource(R.drawable.bg_time_unselected);
        }

        // 3. Coloca a cor no botão novo
        tvClicada.setBackgroundResource(R.drawable.bg_time_selected);
        tvUltimoSelecionado = tvClicada;
        selectedTime = horaEscolhida;

        // 4. Atualiza o texto de confirmação lá embaixo
        if (tvTextoConfirmacao != null) {
            tvTextoConfirmacao.setText("Dia selecionado às " + selectedTime + "h");
        }
    }

    private void atualizarVisualHorarios() {
        for (int id : idsHorarios) {
            TextView tv = findViewById(id);
            if (tv != null) {
                String horaBotao = tv.getText().toString();

                if (horariosOcupados.contains(horaBotao)) {
                    // BLOQUEADO: Tira o clique e deixa transparente
                    tv.setEnabled(false);
                    tv.setAlpha(0.4f);
                    tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
                } else {
                    // LIVRE: Garante que está normal
                    tv.setEnabled(true);
                    tv.setAlpha(1.0f);
                    tv.setBackgroundTintList(null);
                }
            }
        }
    }

    // --- LÓGICA DE AGENDAMENTO (RETROFIT) ---
    private void realizarAgendamento() {
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione um horário primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAgendar.setEnabled(false);
        btnAgendar.setText("AGENDANDO...");

        AgendaRequest request = new AgendaRequest(1, selectedDate, selectedTime);

        RetrofitClient.getApiService().marcarSessao(request).enqueue(new Callback<AgendaResponse>() {
            @Override
            public void onResponse(Call<AgendaResponse> call, Response<AgendaResponse> response) {
                btnAgendar.setEnabled(true);
                btnAgendar.setText("AGENDAR");

                if (response.isSuccessful() && response.body() != null) {
                    AgendaResponse agendaResp = response.body();

                    if (agendaResp.isSucesso()) {
                        Toast.makeText(AgendaActivity.this, "Agendado com sucesso!", Toast.LENGTH_LONG).show();

                        // 🔴 MAGIA ACONTECENDO AQUI:
                        // Adiciona na lista de proibidos e apaga visualmente o botão na hora!
                        horariosOcupados.add(selectedTime);
                        selectedTime = "";
                        tvUltimoSelecionado = null;
                        atualizarVisualHorarios();
                        tvTextoConfirmacao.setText("Selecione um novo horário");

                    } else {
                        Toast.makeText(AgendaActivity.this, "Erro: " + agendaResp.getErro(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AgendaActivity.this, "Erro no servidor ao agendar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AgendaResponse> call, Throwable t) {
                btnAgendar.setEnabled(true);
                btnAgendar.setText("AGENDAR");
                Toast.makeText(AgendaActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- NAVEGAÇÃO INFERIOR ---
    private void configurarNavegacao() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View navInicio = findViewById(R.id.nav_inicio);
        if (navInicio != null) {
            navInicio.setOnClickListener(v -> {
                Intent intent = new Intent(AgendaActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View navExercicios = findViewById(R.id.nav_exercicios);
        if (navExercicios != null) {
            navExercicios.setOnClickListener(v -> startActivity(new Intent(AgendaActivity.this, ExerciciosActivity.class)));
        }

        View navProgresso = findViewById(R.id.nav_progresso);
        if (navProgresso != null) {
            navProgresso.setOnClickListener(v -> startActivity(new Intent(AgendaActivity.this, ProgressoActivity.class)));
        }
    }
}