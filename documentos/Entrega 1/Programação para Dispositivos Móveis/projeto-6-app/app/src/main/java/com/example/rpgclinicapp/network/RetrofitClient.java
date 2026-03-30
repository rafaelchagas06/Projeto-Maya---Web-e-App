package com.example.rpgclinicapp.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // URL do backend no CodeSandbox (Certifique-se que o projeto lá está como PUBLIC)
    private static final String BASE_URL = "https://lnyv32-3000.csb.app/";

    private static Retrofit retrofit = null;

    public static ClinicApiService getApiService() {
        if (retrofit == null) {
            // Adiciona log para ver o que está acontecendo nas requisições
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Criando um cliente OkHttp com logs e timeouts para o CodeSandbox
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
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
