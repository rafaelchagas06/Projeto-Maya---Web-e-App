package com.example.rpgclinicapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rpgclinicapp.models.Prontuario;
import com.example.rpgclinicapp.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressoActivity extends AppCompatActivity {

    private TextView tvStatusAtual, tvNotasMedicas, tvDataNota;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
        setContentView(R.layout.activity_progresso);

        dbHelper = new DatabaseHelper(this);
        tvStatusAtual = findViewById(R.id.tv_status_atual);
        tvNotasMedicas = findViewById(R.id.tv_notas_medicas);
        tvDataNota = findViewById(R.id.tv_data_nota);

        setupBottomNavigation();
        buscarDadosEvolucao();
    }

    private void buscarDadosEvolucao() {
        SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
        long idPaciente = prefs.getLong("idDoUsuario", 0);

        if (idPaciente == 0) return;

        RetrofitClient.getApiService().getProntuarios(idPaciente).enqueue(new Callback<List<Prontuario>>() {
            @Override
            public void onResponse(Call<List<Prontuario>> call, Response<List<Prontuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Prontuario ultimoProntuario = response.body().get(0);
                    atualizarInterface(ultimoProntuario.getObservacao(), ultimoProntuario.getData());
                    salvarNoSQLite(idPaciente, ultimoProntuario);
                } else { carregarDoSQLite(); }
            }
            @Override
            public void onFailure(Call<List<Prontuario>> call, Throwable t) { carregarDoSQLite(); }
        });
    }

    private void atualizarInterface(String observacao, String data) {
        if (tvNotasMedicas != null) tvNotasMedicas.setText(observacao);
        if (tvDataNota != null) tvDataNota.setText("Atualizado em: " + data);
    }

    private void salvarNoSQLite(long idPaciente, Prontuario p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.delete("prontuario_local", null, null);
        values.put("paciente_id", idPaciente);
        values.put("dor", p.getDor());
        values.put("data", p.getData());
        values.put("observacao", p.getObservacao());
        db.insert("prontuario_local", null, values);
        db.close();
    }

    private void carregarDoSQLite() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM prontuario_local LIMIT 1", null);
        if (cursor.moveToFirst()) {
            atualizarInterface(cursor.getString(cursor.getColumnIndexOrThrow("observacao")),
                    cursor.getString(cursor.getColumnIndexOrThrow("data")));
        }
        cursor.close();
        db.close();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_agenda).setOnClickListener(v -> startActivity(new Intent(this, AgendaActivity.class)));
        findViewById(R.id.nav_exercicios).setOnClickListener(v -> startActivity(new Intent(this, ExerciciosActivity.class)));

        // --- ADICIONADO: NAVEGAÇÃO PARA PERFIL ---
        findViewById(R.id.nav_perfil).setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));
    }
}