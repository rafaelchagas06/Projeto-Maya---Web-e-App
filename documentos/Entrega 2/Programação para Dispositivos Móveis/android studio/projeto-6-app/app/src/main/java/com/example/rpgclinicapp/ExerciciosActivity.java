package com.example.rpgclinicapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpgclinicapp.models.CheckinRequest;
import com.example.rpgclinicapp.models.Exercicio;
import com.example.rpgclinicapp.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciciosActivity extends AppCompatActivity {

    private RecyclerView rvExercicios;
    private LinearLayout llHistorico;
    private ExercicioAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_exercicios);

        dbHelper = new DatabaseHelper(this);
        rvExercicios = findViewById(R.id.rv_exercicios);
        llHistorico = findViewById(R.id.ll_historico);

        if (rvExercicios != null) rvExercicios.setLayoutManager(new LinearLayoutManager(this));

        configurarAbas();
        buscarExercicios();
        configurarNavegacao();
        carregarHistorico();
    }

    private void configurarAbas() {
        TextView tvAtivos = findViewById(R.id.tv_aba_ativos);
        TextView tvHistorico = findViewById(R.id.tv_aba_historico);
        View linhaAtivos = findViewById(R.id.linha_ativos);
        View linhaHistorico = findViewById(R.id.linha_historico);

        tvAtivos.setOnClickListener(v -> {
            rvExercicios.setVisibility(View.VISIBLE);
            llHistorico.setVisibility(View.GONE);
            tvAtivos.setTextColor(Color.parseColor("#F07167"));
            tvAtivos.setTypeface(null, Typeface.BOLD);
            linhaAtivos.setBackgroundColor(Color.parseColor("#F07167"));
            tvHistorico.setTextColor(Color.GRAY);
            tvHistorico.setTypeface(null, Typeface.NORMAL);
            linhaHistorico.setBackgroundColor(Color.parseColor("#E0E0E0"));
        });

        tvHistorico.setOnClickListener(v -> {
            rvExercicios.setVisibility(View.GONE);
            llHistorico.setVisibility(View.VISIBLE);
            tvHistorico.setTextColor(Color.parseColor("#F07167"));
            tvHistorico.setTypeface(null, Typeface.BOLD);
            linhaHistorico.setBackgroundColor(Color.parseColor("#F07167"));
            tvAtivos.setTextColor(Color.GRAY);
            tvAtivos.setTypeface(null, Typeface.NORMAL);
            linhaAtivos.setBackgroundColor(Color.parseColor("#E0E0E0"));
            carregarHistorico();
        });
    }

    private void buscarExercicios() {
        SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
        long meuId = prefs.getLong("idDoUsuario", -1);
        if (meuId != -1) {
            RetrofitClient.getApiService().getExercicios(meuId).enqueue(new Callback<List<Exercicio>>() {
                @Override
                public void onResponse(Call<List<Exercicio>> call, Response<List<Exercicio>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Exercicio> lista = response.body();
                        prefs.edit().putInt("totalExercicios", lista.size()).apply();
                        adapter = new ExercicioAdapter(lista, (view, exercicio) -> onMarcarCompleto(view, exercicio));
                        rvExercicios.setAdapter(adapter);
                    }
                }
                @Override public void onFailure(Call<List<Exercicio>> call, Throwable t) {}
            });
        }
    }

    public void onMarcarCompleto(View view, Exercicio exercicio) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_registro_execucao);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.SeekBar sbDor = dialog.findViewById(R.id.sb_dor);
        TextView tvValorDor = dialog.findViewById(R.id.tv_valor_dor);
        EditText etComentario = dialog.findViewById(R.id.et_comentario_exercicio);
        android.widget.Button btnSalvar = dialog.findViewById(R.id.btn_salvar_registro);

        sbDor.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar sb, int p, boolean f) { tvValorDor.setText(String.valueOf(p)); }
            @Override public void onStartTrackingTouch(android.widget.SeekBar sb) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar sb) {}
        });

        btnSalvar.setOnClickListener(v -> {
            String nomeEx = (exercicio != null) ? exercicio.getNome() : "Exercício";
            String comentario = etComentario.getText().toString().trim();
            SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
            long idUser = prefs.getLong("idDoUsuario", 0);
            String nomeUser = prefs.getString("nomeDoUsuario", "Paciente");

            // 1. Mensagem avisando que o app começou a enviar os dados
            Toast.makeText(ExerciciosActivity.this, "Enviando registro...", Toast.LENGTH_SHORT).show();

            // 2. Chamada para a API
            RetrofitClient.getApiService().salvarCheckin(new CheckinRequest(idUser, nomeUser, nomeEx, sbDor.getProgress(), comentario)).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                    if (r.isSuccessful()) {
                        // DEU CERTO NA API!
                        Toast.makeText(ExerciciosActivity.this, "Registrado com sucesso!", Toast.LENGTH_LONG).show();
                    } else {
                        // A API RESPONDEU COM ERRO
                        salvarCheckinNoSQLite(idUser, nomeUser, nomeEx, sbDor.getProgress(), comentario);
                        Toast.makeText(ExerciciosActivity.this, "Salvo offline (Erro no banco)", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                    // ERRO DE INTERNET OU CONEXÃO
                    salvarCheckinNoSQLite(idUser, nomeUser, nomeEx, sbDor.getProgress(), comentario);
                    Toast.makeText(ExerciciosActivity.this, "Salvo offline (Sem internet)", Toast.LENGTH_SHORT).show();
                }
            });

            // 3. Atualização da Interface
            prefs.edit().putInt("exerciciosConcluidos", prefs.getInt("exerciciosConcluidos", 0) + 1).apply();
            salvarNoHistoricoLocal(nomeEx, sbDor.getProgress(), comentario);
            dialog.dismiss();

            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                tv.setText("Completo ✓");
                tv.setBackgroundResource(R.drawable.bg_button_teal);
                tv.setTextColor(Color.WHITE);
                tv.setEnabled(false);
            }
        });
        dialog.show();
    }

    private void salvarNoHistoricoLocal(String nomeEx, int dor, String comentario) {
        SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
        String data = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        String textoComentario = comentario.isEmpty() ? "" : "\nNota: " + comentario;
        String novo = "✓ " + nomeEx + " (Dor: " + dor + ")" + textoComentario + " - " + data;

        String antigo = prefs.getString("historicoExercicios", "");
        prefs.edit().putString("historicoExercicios", antigo.isEmpty() ? novo : novo + "||" + antigo).apply();
        carregarHistorico();
    }

    private void carregarHistorico() {
        if (llHistorico == null) return;
        llHistorico.removeAllViews();
        String historico = getSharedPreferences("MeusDados", MODE_PRIVATE).getString("historicoExercicios", "");
        if (historico.isEmpty()) return;

        for (String registro : historico.split("\\|\\|")) {
            TextView tv = new TextView(this);
            tv.setText(registro);
            tv.setPadding(20, 30, 20, 30);
            tv.setTextColor(Color.parseColor("#333333"));
            tv.setTextSize(14);

            View linha = new View(this);
            linha.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
            linha.setBackgroundColor(Color.LTGRAY);
            llHistorico.addView(tv);
            llHistorico.addView(linha);
        }
    }

    private void salvarCheckinNoSQLite(long id, String nome, String ex, int dor, String comentario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("paciente_id", id);
        v.put("paciente_nome", nome);
        v.put("exercicio_nome", ex);
        v.put("dor", dor);
        v.put("comentario", comentario);
        db.insert("checkins_pendentes", null, v);
        db.close();
    }

    private void configurarNavegacao() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_agenda).setOnClickListener(v -> startActivity(new Intent(this, AgendaActivity.class)));
        findViewById(R.id.nav_progresso).setOnClickListener(v -> startActivity(new Intent(this, ProgressoActivity.class)));
        findViewById(R.id.nav_perfil).setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));
    }
}