package com.example.rpgclinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ProgressoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide top Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_progresso);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Find Navigation buttons
        LinearLayout navInicio = findViewById(R.id.nav_inicio);
        LinearLayout navAgenda = findViewById(R.id.nav_agenda);
        LinearLayout navExercicios = findViewById(R.id.nav_exercicios);

        // Define Listeners
        if (navInicio != null) {
            navInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProgressoActivity.this, MainActivity.class);
                    // Clear backstack logic to avoid infinite activity piling
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }

        if (navAgenda != null) {
            navAgenda.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProgressoActivity.this, AgendaActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (navExercicios != null) {
            navExercicios.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProgressoActivity.this, ExerciciosActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Progresso button is not clickable since we are already here.
    }
}
