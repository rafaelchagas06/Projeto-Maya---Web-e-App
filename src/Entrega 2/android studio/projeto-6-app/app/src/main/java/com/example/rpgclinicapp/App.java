package com.example.rpgclinicapp;

import android.app.Application;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class App extends Application {

    // O seu código do OneSignal!
    private static final String ONESIGNAL_APP_ID = "f7d3db90-9c13-4bc6-82cd-e69ce07636cc";

    @Override
    public void onCreate() {
        super.onCreate();

        // Ajuda a ver erros no painel de controle caso aconteçam
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // Inicializa o motor do OneSignal
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        // Pede permissão para enviar notificações (Obrigatório no Android 13+)
        OneSignal.getNotifications().requestPermission(true, com.onesignal.Continue.with(r -> {}));
    }
}