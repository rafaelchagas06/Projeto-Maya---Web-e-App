package com.example.rpgclinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
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
import java.util.Date;
import java.util.Locale;

public class AgendaActivity extends AppCompatActivity {

    private String selectedDate = "";
    private String selectedTime = "";

    private TextView tvTime1;
    private TextView tvTime2;
    private TextView tvTime3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Oculta a Action Bar superior para o visual de mockup
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_agenda);

        // Botão voltar no topo da tela
        LinearLayout btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // Fecha esta tela e volta para a anterior (Home)
                }
            });
        }

        // Botão "Início" na barra inferior
        LinearLayout navInicio = findViewById(R.id.nav_inicio);
        if (navInicio != null) {
            navInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AgendaActivity.this, MainActivity.class);
                    // Limpa as telas que estavam abertas antes e vai direto para Home
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }
        // Barra de Nav - Ir para Exercícios
        LinearLayout navExercicios = findViewById(R.id.nav_exercicios);
        if (navExercicios != null) {
            navExercicios.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AgendaActivity.this, ExerciciosActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Barra de Nav - Ir para Progresso
        LinearLayout navProgresso = findViewById(R.id.nav_progresso);
        if (navProgresso != null) {
            navProgresso.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AgendaActivity.this, ProgressoActivity.class);
                    startActivity(intent);
                }
            });
        }

        CalendarView calendarView = findViewById(R.id.calendarView);
        tvTime1 = findViewById(R.id.tv_time_1);
        tvTime2 = findViewById(R.id.tv_time_2);
        tvTime3 = findViewById(R.id.tv_time_3);
        Button btnAgendar = findViewById(R.id.btn_agendar);

        // Preenche com a data atual inicialmente
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(new Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Month is 0-based
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            }
        });

        View.OnClickListener timeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimeSelection();
                v.setBackgroundResource(R.drawable.bg_time_selected);
                selectedTime = ((TextView) v).getText().toString(); // Ex: "10:30"
            }
        };

        tvTime1.setOnClickListener(timeClickListener);
        tvTime2.setOnClickListener(timeClickListener);
        tvTime3.setOnClickListener(timeClickListener);

        btnAgendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTime.isEmpty()) {
                    Toast.makeText(AgendaActivity.this, "Por favor, selecione um horário primeiro.", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                btnAgendar.setEnabled(false);
                btnAgendar.setText("AGENDANDO...");

                // ID_PACIENTE mockado como 1 temporariamente para o teste de fluxo
                AgendaRequest request = new AgendaRequest(1, selectedDate, selectedTime);

                RetrofitClient.getApiService().marcarSessao(request).enqueue(new Callback<AgendaResponse>() {
                    @Override
                    public void onResponse(Call<AgendaResponse> call, Response<AgendaResponse> response) {
                        btnAgendar.setEnabled(true);
                        btnAgendar.setText("AGENDAR");

                        if (response.isSuccessful() && response.body() != null) {
                            AgendaResponse agendaResp = response.body();
                            if (agendaResp.isSucesso()) {
                                Toast.makeText(AgendaActivity.this,
                                        "Agendado para " + agendaResp.getDetalhes().getData() + " às "
                                                + agendaResp.getDetalhes().getHorario(),
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(AgendaActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(AgendaActivity.this, "Erro: " + agendaResp.getErro(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            Toast.makeText(AgendaActivity.this, "Erro no servidor ao agendar", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AgendaResponse> call, Throwable t) {
                        btnAgendar.setEnabled(true);
                        btnAgendar.setText("AGENDAR");
                        Toast.makeText(AgendaActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
        });
    }

    private void resetTimeSelection() {
        tvTime1.setBackgroundResource(R.drawable.bg_time_unselected);
        tvTime2.setBackgroundResource(R.drawable.bg_time_unselected);
        tvTime3.setBackgroundResource(R.drawable.bg_time_unselected);
    }
}
