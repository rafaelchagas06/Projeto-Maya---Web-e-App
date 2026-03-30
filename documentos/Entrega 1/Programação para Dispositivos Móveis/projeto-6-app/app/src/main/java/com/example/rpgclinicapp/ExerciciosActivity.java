package com.example.rpgclinicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class ExerciciosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove a Action Bar no topo
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_exercicios);

        // Barra de Nav - Ir para Início
        LinearLayout navInicio = findViewById(R.id.nav_inicio);
        if (navInicio != null) {
            navInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ExerciciosActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }

        // Barra de Nav - Ir para Agenda
        LinearLayout navAgenda = findViewById(R.id.nav_agenda);
        if (navAgenda != null) {
            navAgenda.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ExerciciosActivity.this, AgendaActivity.class);
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
                    Intent intent = new Intent(ExerciciosActivity.this, ProgressoActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
    
    public void onMarcarCompleto(View view) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_registro_execucao);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        
        android.widget.SeekBar sbDor = dialog.findViewById(R.id.sb_dor);
        android.widget.TextView tvValorDor = dialog.findViewById(R.id.tv_valor_dor);
        android.widget.EditText etObservacoes = dialog.findViewById(R.id.et_observacoes);
        android.widget.Button btnSalvar = dialog.findViewById(R.id.btn_salvar_registro);
        
        sbDor.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                tvValorDor.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        btnSalvar.setOnClickListener(v -> {
            String dor = tvValorDor.getText().toString();
            android.widget.Toast.makeText(this, "Registro salvo! Dor: " + dor, android.widget.Toast.LENGTH_SHORT).show();
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
