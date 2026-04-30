package com.example.rpgclinicapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressCircular;
    private TextView textPorcentagemCentral;
    private TextView textContadorSuperior;

    // Variável para guardar a dor selecionada
    private int nivelDorSelecionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Oculta a Action Bar superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        // --- Inicialização dos Componentes do Círculo de Progresso ---
        progressCircular = findViewById(R.id.progressCircular);
        textPorcentagemCentral = findViewById(R.id.textPorcentagemCentral);
        textContadorSuperior = findViewById(R.id.textContadorExercicios);

        // --- Lógica da Saudação e Data Dinâmica  ---
        configurarSaudacaoEData();

        // --- Lógica de Notificações ---
        criarCanalNotificacao();
        solicitarPermissaoEAgendarLembrete();

        // --- Lógica de Escala de Dor ---
        configurarEscalaDor();

        // --- Configuração da Barra Inferior ---
        configurarNavegacao();
    }

    // O onResume roda toda vez que você volta para esta tela
    @Override
    protected void onResume() {
        super.onResume();

        // Recupera os dados que você salvou na ExerciciosActivity
        android.content.SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
        int feitos = prefs.getInt("exerciciosConcluidos", 0);

        // Defini 3 como exemplo porque você disse que a Maya colocou 3 exercícios
        // Se ela colocar mais, mude esse 3 para o número total.
        int total = 3;

        // Atualiza a interface com os dados reais salvos
        atualizarInterfaceProgresso(feitos, total);
    }

    // --- Função para atualizar o Círculo e os Textos ---
    private void atualizarInterfaceProgresso(int concluidos, int total) {
        if (total <= 0) return; // Evita divisão por zero

        // Calcula a porcentagem
        int porcentagem = (concluidos * 100) / total;

        // Garante que a porcentagem não passe de 100%
        if (porcentagem > 100) porcentagem = 100;

        // Atualiza a barra visual
        if (progressCircular != null) {
            progressCircular.setProgress(porcentagem);
        }

        // Atualiza o texto "0%"
        if (textPorcentagemCentral != null) {
            textPorcentagemCentral.setText(porcentagem + "%");
        }

        // Atualiza o contador "0/10"
        if (textContadorSuperior != null) {
            textContadorSuperior.setText(concluidos + "/" + total);
        }
    }

    private void configurarSaudacaoEData() {
        TextView textSaudacao = findViewById(R.id.textSaudacao);
        TextView textData = findViewById(R.id.textData);

        if (textSaudacao != null && textData != null) {
            java.util.Calendar calendario = java.util.Calendar.getInstance();
            int horaAtual = calendario.get(java.util.Calendar.HOUR_OF_DAY);
            String saudacao;

            if (horaAtual >= 6 && horaAtual < 12) {
                saudacao = "Bom dia";
            } else if (horaAtual >= 12 && horaAtual < 18) {
                saudacao = "Boa tarde";
            } else {
                saudacao = "Boa noite";
            }

            android.content.SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
            String nomeUsuario = prefs.getString("nomeDoUsuario", "Paciente");

            textSaudacao.setText(saudacao + ", " + nomeUsuario + "!");

            java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("EEEE, dd 'de' MMMM", new java.util.Locale("pt", "BR"));
            String dataAtual = formatadorData.format(new java.util.Date());
            dataAtual = dataAtual.substring(0, 1).toUpperCase() + dataAtual.substring(1) + ".";

            textData.setText(dataAtual);
        }
    }

    private void configurarEscalaDor() {
        final int[] idsBotoesDor = {
                R.id.dor_1, R.id.dor_2, R.id.dor_3, R.id.dor_4, R.id.dor_5,
                R.id.dor_6, R.id.dor_7, R.id.dor_8, R.id.dor_9, R.id.dor_10
        };

        for (int i = 0; i < idsBotoesDor.length; i++) {
            final int nivel = i + 1;
            final TextView tvDor = findViewById(idsBotoesDor[i]);

            if (tvDor != null) {
                tvDor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nivelDorSelecionado = nivel;
                        Toast.makeText(MainActivity.this, "Dor nível " + nivel, Toast.LENGTH_SHORT).show();

                        for (int id : idsBotoesDor) {
                            View b = findViewById(id);
                            if (b != null) {
                                b.setScaleX(1.0f);
                                b.setScaleY(1.0f);
                                b.setAlpha(0.6f);
                            }
                        }
                        v.setScaleX(1.2f);
                        v.setScaleY(1.2f);
                        v.setAlpha(1.0f);
                    }
                });
            }
        }
    }

    private void configurarNavegacao() {
        android.widget.LinearLayout navAgenda = findViewById(R.id.nav_agenda);
        if (navAgenda != null) {
            navAgenda.setOnClickListener(v -> {
                startActivity(new android.content.Intent(MainActivity.this, AgendaActivity.class));
            });
        }

        android.widget.LinearLayout navExercicios = findViewById(R.id.nav_exercicios);
        if (navExercicios != null) {
            navExercicios.setOnClickListener(v -> {
                startActivity(new android.content.Intent(MainActivity.this, ExerciciosActivity.class));
            });
        }

        android.widget.LinearLayout navProgresso = findViewById(R.id.nav_progresso);
        if (navProgresso != null) {
            navProgresso.setOnClickListener(v -> {
                startActivity(new android.content.Intent(MainActivity.this, ProgressoActivity.class));
            });
        }
    }

    private void criarCanalNotificacao() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Lembrete RPG";
            String description = "Canal para lembretes de exercícios";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel channel = new android.app.NotificationChannel("RPG_ALARM_CHANNEL", name, importance);
            channel.setDescription(description);
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
    }

    private void solicitarPermissaoEAgendarLembrete() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        agendarLembreteDiario();
    }

    private void agendarLembreteDiario() {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(android.content.Context.ALARM_SERVICE);
        android.content.Intent intent = new android.content.Intent(this, LembreteReceiver.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 10);
        calendar.set(java.util.Calendar.MINUTE, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (alarmManager != null) {
                alarmManager.setRepeating(android.app.AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), android.app.AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}