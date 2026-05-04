package com.example.rpgclinicapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpgclinicapp.models.Exercicio;
import com.example.rpgclinicapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciciosActivity extends AppCompatActivity {

    private RecyclerView rvExercicios;
    private ExercicioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_exercicios);

        // 1. Configurar o RecyclerView (Lista)
        rvExercicios = findViewById(R.id.rv_exercicios);
        if (rvExercicios != null) {
            rvExercicios.setLayoutManager(new LinearLayoutManager(this));
        }

        // 2. Chamar a API para buscar os exercícios
        buscarExercicios();

        // 3. Configurar a Navegação
        configurarNavegacao();
    }

    private void buscarExercicios() {
        RetrofitClient.getApiService().getExercicios().enqueue(new Callback<List<Exercicio>>() {
            @Override
            public void onResponse(Call<List<Exercicio>> call, Response<List<Exercicio>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercicio> lista = response.body();

                    // Configura o Adapter com a lista que veio do servidor
                    adapter = new ExercicioAdapter(lista, (view, exercicio) -> {
                        // Quando clicar no botão da lista, abre o seu Dialog de Dor
                        onMarcarCompleto(view);
                    });

                    rvExercicios.setAdapter(adapter);
                } else {
                    Toast.makeText(ExerciciosActivity.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Exercicio>> call, Throwable t) {
                Toast.makeText(ExerciciosActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configurarNavegacao() {
        LinearLayout navInicio = findViewById(R.id.nav_inicio);
        if (navInicio != null) {
            navInicio.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        LinearLayout navAgenda = findViewById(R.id.nav_agenda);
        if (navAgenda != null) {
            navAgenda.setOnClickListener(v -> startActivity(new Intent(this, AgendaActivity.class)));
        }

        LinearLayout navProgresso = findViewById(R.id.nav_progresso);
        if (navProgresso != null) {
            navProgresso.setOnClickListener(v -> startActivity(new Intent(this, ProgressoActivity.class)));
        }
    }

    // Sua lógica original do Dialog de Dor
    public void onMarcarCompleto(View view) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_registro_execucao);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.SeekBar sbDor = dialog.findViewById(R.id.sb_dor);
        android.widget.TextView tvValorDor = dialog.findViewById(R.id.tv_valor_dor);
        android.widget.Button btnSalvar = dialog.findViewById(R.id.btn_salvar_registro);

        sbDor.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                tvValorDor.setText(String.valueOf(progress));
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });

        btnSalvar.setOnClickListener(v -> {
            String dor = tvValorDor.getText().toString();
            SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
            int concluidosAtual = prefs.getInt("exerciciosConcluidos", 0);
            prefs.edit().putInt("exerciciosConcluidos", concluidosAtual + 1).apply();

            Toast.makeText(this, "Registro salvo! Dor: " + dor, Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            if (view instanceof android.widget.TextView) {
                android.widget.TextView tv = (android.widget.TextView) view;
                tv.setText("Completo ✓");
                tv.setBackgroundResource(R.drawable.bg_button_teal);
                tv.setTextColor(android.graphics.Color.WHITE);
                tv.setEnabled(false);
            }
        });

        dialog.show();
    }
}