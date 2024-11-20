package com.example.ia;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static String baseUrl = "";  // Almacena la URL base actual

    public static Retrofit getClient(String newBaseUrl) {
        // Si retrofit es nulo o si la URL base ha cambiado, crea una nueva instancia de Retrofit
        if (retrofit == null || !baseUrl.equals(newBaseUrl)) {
            baseUrl = newBaseUrl;  // Actualiza la URL base
            retrofit = new Retrofit.Builder()
                    .baseUrl(newBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
