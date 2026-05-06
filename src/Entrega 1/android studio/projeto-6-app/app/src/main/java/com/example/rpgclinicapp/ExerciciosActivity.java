package com.example.rpgclinicapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpgclinicapp.models.CheckinRequest;
import com.example.rpgclinicapp.models.Exercicio;
import com.example.rpgclinicapp.network.RetrofitClient;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciciosActivity extends AppCompatActivity {

    private RecyclerView rvExercicios;
    private ExercicioAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_exercicios);

        dbHelper = new DatabaseHelper(this);

        rvExercicios = findViewById(R.id.rv_exercicios);
        if (rvExercicios != null) {
            rvExercicios.setLayoutManager(new LinearLayoutManager(this));
        }

        buscarExercicios();
        configurarNavegacao();
    }

    private void buscarExercicios() {
        RetrofitClient.getApiService().getExercicios().enqueue(new Callback<List<Exercicio>>() {
            @Override
            public void onResponse(Call<List<Exercicio>> call, Response<List<Exercicio>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercicio> lista = response.body();
                    adapter = new ExercicioAdapter(lista, (view, exercicio) -> {
                        onMarcarCompleto(view, exercicio);
                    });
                    rvExercicios.setAdapter(adapter);
                } else {
                    Toast.makeText(ExerciciosActivity.this, "Erro ao carregar exercícios", Toast.LENGTH_SHORT).show();
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

        // --- ADICIONADO: NAVEGAÇÃO PARA PERFIL ---
        LinearLayout navPerfil = findViewById(R.id.nav_perfil);
        if (navPerfil != null) {
            navPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(this, PerfilActivity.class);
                startActivity(intent);
            });
        }
    }

    public void onMarcarCompleto(View view, Exercicio exercicio) {
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
            int nivelDor = sbDor.getProgress();
            String nomeEx = (exercicio != null) ? exercicio.getNome() : "Exercício";

            SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
            String nomeUsuarioLogado = prefs.getString("nomeDoUsuario", "Paciente");
            long idUsuarioLogado = prefs.getLong("idDoUsuario", 0);

            CheckinRequest checkin = new CheckinRequest(idUsuarioLogado, nomeUsuarioLogado, nomeEx, nivelDor);

            RetrofitClient.getApiService().salvarCheckin(checkin).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ExerciciosActivity.this, "✅ Registro enviado com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        salvarCheckinNoSQLite(idUsuarioLogado, nomeUsuarioLogado, nomeEx, nivelDor);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    salvarCheckinNoSQLite(idUsuarioLogado, nomeUsuarioLogado, nomeEx, nivelDor);
                    Toast.makeText(ExerciciosActivity.this, "⚠️ Offline: Salvo localmente no celular", Toast.LENGTH_SHORT).show();
                }
            });

            int concluidosAtual = prefs.getInt("exerciciosConcluidos", 0);
            prefs.edit().putInt("exerciciosConcluidos", concluidosAtual + 1).apply();

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

    private void salvarCheckinNoSQLite(long idPaciente, String nomePaciente, String exercicio, int dor) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("paciente_id", idPaciente);
            values.put("paciente_nome", nomePaciente);
            values.put("exercicio_nome", exercicio);
            values.put("dor", dor);

            db.insert("checkins_pendentes", null, values);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}