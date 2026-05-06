package com.example.rpgclinicapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
        setContentView(R.layout.activity_perfil);

        TextView tvNome = findViewById(R.id.tv_nome_perfil);
        LinearLayout btnVisualizar = findViewById(R.id.btn_visualizar_dados);
        LinearLayout btnLogout = findViewById(R.id.btn_logout);

        // 1. Pegar dados do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
        String nome = prefs.getString("nomeDoUsuario", "Usuário");
        String email = prefs.getString("emailDoUsuario", "nao_informado@teste.com");
        String senha = prefs.getString("senhaDoUsuario", "*******");

        tvNome.setText(nome);

        // 2. Ação de Visualizar Dados (Apenas mostra um alerta com as informações)
        btnVisualizar.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Dados do Perfil");
            builder.setMessage("Nome: " + nome + "\nEmail: " + email + "\nSenha: " + senha);
            builder.setPositiveButton("Fechar", null);
            builder.show();
        });

        // 3. Lógica de Logout
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_agenda).setOnClickListener(v -> startActivity(new Intent(this, AgendaActivity.class)));
        findViewById(R.id.nav_exercicios).setOnClickListener(v -> startActivity(new Intent(this, ExerciciosActivity.class)));
        findViewById(R.id.nav_progresso).setOnClickListener(v -> startActivity(new Intent(this, ProgressoActivity.class)));
    }
}