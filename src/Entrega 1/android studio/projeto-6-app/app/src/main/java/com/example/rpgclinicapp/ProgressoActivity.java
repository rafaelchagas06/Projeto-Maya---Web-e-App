package com.example.rpgclinicapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rpgclinicapp.models.Prontuario;
import com.example.rpgclinicapp.network.RetrofitClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressoActivity extends AppCompatActivity {

    private TextView tvNotasMedicas, tvDataNota, tvNomePerfil;
    private LineChart chartDor;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
        setContentView(R.layout.activity_progresso);

        dbHelper = new DatabaseHelper(this);
        tvNotasMedicas = findViewById(R.id.tv_notas_medicas);
        tvDataNota = findViewById(R.id.tv_data_nota);
        tvNomePerfil = findViewById(R.id.tv_nome_perfil);
        chartDor = findViewById(R.id.chart_dor);

        SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
        String nomeUser = prefs.getString("nomeDoUsuario", "Paciente");
        if (tvNomePerfil != null) tvNomePerfil.setText(nomeUser);

        setupBottomNavigation();
        configurarEstiloGrafico();
        buscarDadosEvolucao();
    }

    private void configurarEstiloGrafico() {
        if (chartDor == null) return;
        chartDor.getDescription().setEnabled(false);
        chartDor.setDrawGridBackground(false);
        chartDor.setTouchEnabled(true);
        chartDor.setNoDataText("Carregando dados do gráfico...");

        chartDor.getAxisLeft().setAxisMaximum(10f);
        chartDor.getAxisLeft().setAxisMinimum(0f);
        chartDor.getAxisLeft().setGranularity(1f);
        chartDor.getAxisRight().setEnabled(false);

        XAxis xAxis = chartDor.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void buscarDadosEvolucao() {
        SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
        long idPaciente = prefs.getLong("idDoUsuario", 0);
        if (idPaciente == 0) return;

        RetrofitClient.getApiService().getProntuarios(idPaciente).enqueue(new Callback<List<Prontuario>>() {
            @Override
            public void onResponse(Call<List<Prontuario>> call, Response<List<Prontuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Prontuario> lista = response.body();
                    salvarHistoricoNoSQLite(idPaciente, lista);
                    carregarNotasDoSQLite();
                    gerarGrafico();
                } else {
                    carregarNotasDoSQLite();
                    gerarGrafico();
                }
            }
            @Override
            public void onFailure(Call<List<Prontuario>> call, Throwable t) {
                carregarNotasDoSQLite();
                gerarGrafico();
            }
        });
    }

    private void gerarGrafico() {
        if (chartDor == null) return;

        List<Entry> entradas = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT dor FROM prontuario_local ORDER BY data ASC", null);

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                float nivelDor = cursor.getFloat(0);
                entradas.add(new Entry(count, nivelDor));
                count++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        if (entradas.isEmpty()) return;

        LineDataSet dataSet = new LineDataSet(entradas, "Evolução da Dor");
        dataSet.setColor(Color.parseColor("#F07167"));
        dataSet.setCircleColor(Color.parseColor("#F07167"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#F07167"));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chartDor.setData(lineData);
        chartDor.animateX(800);
        chartDor.invalidate();
    }

    // --- MUDANÇA PRINCIPAL AQUI ---
    private void carregarNotasDoSQLite() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT observacao, data FROM prontuario_local ORDER BY data DESC", null);

        StringBuilder acumulado = new StringBuilder();
        String dataMaisRecente = "---";

        if (cursor.moveToFirst()) {
            // Formata a data do cabeçalho
            dataMaisRecente = formatarDataBrasil(cursor.getString(1));
            do {
                String obs = cursor.getString(0);
                // Formata a data de cada item da lista
                String dataFormatada = formatarDataBrasil(cursor.getString(1));

                acumulado.append("📅 ").append(dataFormatada).append("\n")
                        .append(obs).append("\n")
                        .append("----------------------------\n\n");
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        if (tvNotasMedicas != null) tvNotasMedicas.setText(acumulado.toString());
        if (tvDataNota != null) tvDataNota.setText("Atualizado em: " + dataMaisRecente);
    }

    // --- FUNÇÃO NOVA PARA CORTAR O "T" E FORMATAR ---
    private String formatarDataBrasil(String dataBanco) {
        if (dataBanco == null || dataBanco.isEmpty()) return "";
        try {
            String dataSemHora = dataBanco.split("T")[0];
            java.text.SimpleDateFormat formatoOriginal = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat formatoBrasil = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.util.Date dataConvertida = formatoOriginal.parse(dataSemHora);
            return formatoBrasil.format(dataConvertida);
        } catch (Exception e) {
            return dataBanco; // Se der erro, mostra original para não travar
        }
    }

    private void salvarHistoricoNoSQLite(long idPaciente, List<Prontuario> lista) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("prontuario_local", null, null);

        for (Prontuario p : lista) {
            ContentValues values = new ContentValues();
            values.put("paciente_id", idPaciente);
            values.put("dor", p.getDor());
            values.put("data", p.getData());
            values.put("observacao", p.getObservacao());
            db.insert("prontuario_local", null, values);
        }
        db.close();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        findViewById(R.id.nav_agenda).setOnClickListener(v -> startActivity(new Intent(this, AgendaActivity.class)));
        findViewById(R.id.nav_exercicios).setOnClickListener(v -> startActivity(new Intent(this, ExerciciosActivity.class)));
        findViewById(R.id.nav_perfil).setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));
    }
}