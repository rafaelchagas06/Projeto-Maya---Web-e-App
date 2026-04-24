package com.example.rpgclinicapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

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

        // --- Lógica da Saudação e Data Dinâmica  ---
        configurarSaudacaoEData();

        // --- Lógica de Notificações ---
        criarCanalNotificacao();
        solicitarPermissaoEAgendarLembrete();

        // --- Lógica da Escala de Dor ---
        configurarEscalaDor();

        // --- Configuração da Barra Inferior ---
        configurarNavegacao();
    }

    private void configurarSaudacaoEData() {
        TextView textSaudacao = findViewById(R.id.textSaudacao);
        TextView textData = findViewById(R.id.textData);

        if (textSaudacao != null && textData != null) {
            //  Lógica da Saudação (Bom dia, Boa tarde, Boa noite)
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

            // . RECUPERA O NOME DO USUÁRIO (Essa é a parte importante!)
            android.content.SharedPreferences prefs = getSharedPreferences("MeusDados", MODE_PRIVATE);
            // Se ele não achar nenhum nome, ele vai escrever "Paciente" para não ficar em branco
            String nomeUsuario = prefs.getString("nomeDoUsuario", "Paciente");

            textSaudacao.setText(saudacao + ", " + nomeUsuario + "!");

            //  Lógica da Data formatada
            java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("EEEE, dd 'de' MMMM", new java.util.Locale("pt", "BR"));
            String dataAtual = formatadorData.format(new java.util.Date());
            dataAtual = dataAtual.substring(0, 1).toUpperCase() + dataAtual.substring(1) + ".";

            textData.setText(dataAtual);
        }
    }

    private void configurarEscalaDor() {
        // IDs dos números
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

                        // Feedback visual e sonoro rápido
                        Toast.makeText(MainActivity.this, "Dor nível " + nivel, Toast.LENGTH_SHORT).show();

                        // Reseta o tamanho de todos e destaca o clicado
                        for (int id : idsBotoesDor) {
                            View b = findViewById(id);
                            b.setScaleX(1.0f);
                            b.setScaleY(1.0f);
                            b.setAlpha(0.6f); // Deixa os outros levemente transparentes
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
        // Agenda
        android.widget.LinearLayout navAgenda = findViewById(R.id.nav_agenda);
        if (navAgenda != null) {
            navAgenda.setOnClickListener(v -> {
                startActivity(new android.content.Intent(MainActivity.this, AgendaActivity.class));
            });
        }

        // Exercícios
        android.widget.LinearLayout navExercicios = findViewById(R.id.nav_exercicios);
        if (navExercicios != null) {
            navExercicios.setOnClickListener(v -> {
                startActivity(new android.content.Intent(MainActivity.this, ExerciciosActivity.class));
            });
        }

        // Progresso
        android.widget.LinearLayout navProgresso = findViewById(R.id.nav_progresso);
        if (navProgresso != null) {
            navProgresso.setOnClickListener(v -> {
                startActivity(new android.content.Intent(MainActivity.this, ProgressoActivity.class));
            });
        }
    }

    // --- Seus métodos originais de Notificação (Mantidos) ---

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