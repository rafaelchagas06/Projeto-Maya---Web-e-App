package com.example.rpgclinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // <--- IMPORTANTE: Adicionado para ajudar no debug
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rpgclinicapp.models.LoginRequest;
import com.example.rpgclinicapp.models.LoginResponse;
import com.example.rpgclinicapp.network.RetrofitClient;

import org.mindrot.jbcrypt.BCrypt;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                String senhaDigitada = etPassword.getText().toString().trim();

                if (email.isEmpty() || senhaDigitada.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnEntrar.setEnabled(false);
                btnEntrar.setText("CARREGANDO...");

                LoginRequest request = new LoginRequest(email, senhaDigitada);
                RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        btnEntrar.setEnabled(true);
                        btnEntrar.setText("ENTRAR");

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResp = response.body();
                            if (loginResp.isSucesso()) {

                                // --- INÍCIO DA ALTERAÇÃO ---
                                // O .trim() garante que espaços invisíveis vindos do banco sejam ignorados
                                String senhaHasheadaDoBanco = loginResp.getUsuario().getSenha().trim();

                                // Imprime no Logcat o que o Android está vendo para facilitar achar o erro
                                Log.d("DEBUG_LOGIN", "Senha Digitada: [" + senhaDigitada + "]");
                                Log.d("DEBUG_LOGIN", "Hash do Banco: [" + senhaHasheadaDoBanco + "]");

                                try {
                                    // Verifica se a senha digitada combina com o Hash do Supabase
                                    if (BCrypt.checkpw(senhaDigitada, senhaHasheadaDoBanco)) {

                                        String nomeDaPessoa = loginResp.getUsuario().getNome();
                                        long idDaPessoa = loginResp.getUsuario().getId();

                                        android.content.SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
                                        android.content.SharedPreferences.Editor editor = prefs.edit();

                                        editor.putString("nomeDoUsuario", nomeDaPessoa);
                                        editor.putLong("idDoUsuario", idDaPessoa);
                                        editor.putString("emailDoUsuario", email);
                                        editor.putString("senhaDoUsuario", senhaDigitada);
                                        editor.apply();

                                        Toast.makeText(LoginActivity.this, "Bem-vindo(a), " + nomeDaPessoa, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(LoginActivity.this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    // Mostra no Log e na tela o motivo exato da falha do BCrypt
                                    Log.e("DEBUG_LOGIN", "Erro BCrypt: " + e.getMessage());
                                    Toast.makeText(LoginActivity.this, "Erro formato: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                // --- FIM DA ALTERAÇÃO ---

                            } else {
                                Toast.makeText(LoginActivity.this, "Erro: " + loginResp.getErro(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        btnEntrar.setEnabled(true);
                        btnEntrar.setText("ENTRAR");
                        Toast.makeText(LoginActivity.this, "Falha na conexão", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        android.widget.TextView tvEsqueciSenha = findViewById(R.id.tv_esqueci_senha);
        if (tvEsqueciSenha != null) {
            tvEsqueciSenha.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RecuperarSenhaActivity.class);
                startActivity(intent);
            });
        }
    }
}