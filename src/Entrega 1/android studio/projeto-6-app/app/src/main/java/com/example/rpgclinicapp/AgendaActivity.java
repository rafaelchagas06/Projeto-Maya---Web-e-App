package com.example.rpgclinicapp;

import android.content.Intent;
import android.content.SharedPreferences;
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

    // Lista para simular horários que já estão ocupados
    private List<String> horariosOcupados = new ArrayList<>();

    // IDs dos horários do seu XML
    private final int[] idsHorarios = {
            R.id.hora_0800, R.id.hora_0900, R.id.hora_1000,
            R.id.hora_1100, R.id.hora_1300, R.id.hora_1400,
            R.id.hora_1500, R.id.hora_1600, R.id.hora_1700
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_agenda);

        tvTextoConfirmacao = findViewById(R.id.tv_texto_confirmacao);
        btnAgendar = findViewById(R.id.btn_agendar);
        CalendarView calendarView = findViewById(R.id.calendarView);

        // Configuração Inicial da Data (Padrão Banco: yyyy-MM-dd)
        SimpleDateFormat sdfBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdfBanco.format(new Date(calendarView.getDate()));

        // 1. Carrega os horários ocupados do dia de hoje assim que abre a tela
        carregarHorariosOcupados(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Salva no formato do banco
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

            // 2. Sempre que mudar de dia, carrega os horários daquele dia específico
            carregarHorariosOcupados(selectedDate);

            // Limpa a seleção visual quando troca de dia
            selectedTime = "";
            if (tvUltimoSelecionado != null) {
                tvUltimoSelecionado.setBackgroundResource(R.drawable.bg_time_unselected);
                tvUltimoSelecionado = null;
            }
            tvTextoConfirmacao.setText("Selecione um horário");
        });

        configurarNavegacao();
        btnAgendar.setOnClickListener(v -> realizarAgendamento());
    }

    public void selecionarHora(View view) {
        TextView tvClicada = (TextView) view;
        String horaEscolhida = tvClicada.getText().toString();

        if (horariosOcupados.contains(horaEscolhida)) {
            Toast.makeText(this, "Horário indisponível neste dia!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tvUltimoSelecionado != null) {
            tvUltimoSelecionado.setBackgroundResource(R.drawable.bg_time_unselected);
        }

        tvClicada.setBackgroundResource(R.drawable.bg_time_selected);
        tvUltimoSelecionado = tvClicada;
        selectedTime = horaEscolhida;

        // --- CONVERSÃO DE DATA PARA BR NO TEXTO ---
        if (tvTextoConfirmacao != null) {
            try {
                SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat formatoBR = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date dataObj = formatoBanco.parse(selectedDate);
                String dataFormatadaBR = formatoBR.format(dataObj);

                tvTextoConfirmacao.setText("Dia " + dataFormatadaBR + " às " + selectedTime + "h");
            } catch (Exception e) {
                tvTextoConfirmacao.setText("Dia " + selectedDate + " às " + selectedTime + "h");
            }
        }
    }

    private void atualizarVisualHorarios() {
        for (int id : idsHorarios) {
            TextView tv = findViewById(id);
            if (tv != null) {
                String horaBotao = tv.getText().toString();
                if (horariosOcupados.contains(horaBotao)) {
                    tv.setEnabled(false);
                    tv.setAlpha(0.4f);
                    tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
                } else {
                    tv.setEnabled(true);
                    tv.setAlpha(1.0f);
                    tv.setBackgroundTintList(null);
                }
            }
        }
    }

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

                        // 3. Salva definitivamente o horário para este dia no celular
                        salvarHorarioOcupado(selectedDate, selectedTime);

                        selectedTime = "";
                        tvUltimoSelecionado = null;
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

    // --- MÉTODOS NOVOS PARA SALVAR OS HORÁRIOS LOCALMENTE ---

    private void salvarHorarioOcupado(String data, String hora) {
        SharedPreferences prefs = getSharedPreferences("AgendaLocal", MODE_PRIVATE);
        // Pega os horários que já estavam ocupados neste dia
        String ocupados = prefs.getString("ocupados_" + data, "");

        // Adiciona o novo horário (separado por vírgula)
        if (!ocupados.contains(hora)) {
            prefs.edit().putString("ocupados_" + data, ocupados + hora + ",").apply();
        }
        // Recarrega a tela para ficar cinza
        carregarHorariosOcupados(data);
    }

    private void carregarHorariosOcupados(String data) {
        horariosOcupados.clear();
        SharedPreferences prefs = getSharedPreferences("AgendaLocal", MODE_PRIVATE);
        String ocupados = prefs.getString("ocupados_" + data, "");

        if (!ocupados.isEmpty()) {
            // Separa pela vírgula e adiciona na lista
            String[] arrayOcupados = ocupados.split(",");
            for (String h : arrayOcupados) {
                if (!h.isEmpty()) horariosOcupados.add(h);
            }
        }
        // Chama a função que pinta de cinza
        atualizarVisualHorarios();
    }

    // --------------------------------------------------------

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