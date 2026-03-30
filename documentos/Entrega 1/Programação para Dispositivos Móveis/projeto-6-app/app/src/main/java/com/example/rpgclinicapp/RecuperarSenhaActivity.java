package com.example.rpgclinicapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecuperarSenhaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_recuperar_senha);

        EditText etEmail = findViewById(R.id.et_email_recuperar);
        Button btnEnviarLink = findViewById(R.id.btn_enviar_link);
        TextView tvVoltarLogin = findViewById(R.id.tv_voltar_login_recuperar);

        tvVoltarLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEnviarLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(RecuperarSenhaActivity.this, "Digite seu email.", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnEnviarLink.setEnabled(false);
                btnEnviarLink.setText("ENVIANDO...");

                // Simulate network latency for mockup
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RecuperarSenhaActivity.this, "Link de recuperação enviado!", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                }, 1500);
            }
        });
    }
}
