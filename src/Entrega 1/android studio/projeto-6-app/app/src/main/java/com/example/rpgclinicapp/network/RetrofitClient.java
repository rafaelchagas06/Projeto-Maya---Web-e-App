package com.example.rpgclinicapp.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String BASE_URL = "https://projeto6.onrender.com/";
    private static Retrofit retrofit = null;

    public static ClinicApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Aumentamos os timeouts para 90 segundos para dar tempo do Render ligar
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(90, TimeUnit.SECONDS) // Tempo para conectar
                    .readTimeout(90, TimeUnit.SECONDS)    // Tempo para ler a resposta
                    .writeTimeout(90, TimeUnit.SECONDS)   // Tempo para enviar dados
                    .retryOnConnectionFailure(true)       // Tenta reconectar se falhar
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ClinicApiService.class);
    }
}