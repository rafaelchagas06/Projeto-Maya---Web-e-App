package com.example.rpgclinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rpgclinicapp.models.LoginRequest;
import com.example.rpgclinicapp.models.LoginResponse;
import com.example.rpgclinicapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Oculta a Action Bar superior para o visual de mockup
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnEntrar = findViewById(R.id.btn_entrar);

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String senha = etPassword.getText().toString().trim();

                if (email.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                btnEntrar.setEnabled(false);
                btnEntrar.setText("CARREGANDO...");

                LoginRequest request = new LoginRequest(email, senha);
                RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        btnEntrar.setEnabled(true);
                        btnEntrar.setText("ENTRAR");

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResp = response.body();
                            if (loginResp.isSucesso()) {
                                Toast.makeText(LoginActivity.this, "Bem-vindo(a), " + loginResp.getUsuario().getNome(),
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Erro: " + loginResp.getErro(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            // Se a API retornar status code de erro (ex: 401 Unauthorized)
                            Toast.makeText(LoginActivity.this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        btnEntrar.setEnabled(true);
                        btnEntrar.setText("ENTRAR");
                        Toast.makeText(LoginActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
        });

        android.widget.TextView tvEsqueciSenha = findViewById(R.id.tv_esqueci_senha);
        if (tvEsqueciSenha != null) {
            tvEsqueciSenha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, RecuperarSenhaActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
