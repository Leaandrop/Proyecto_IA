package com.example.ia;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("predict")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part image);

    // En la creación de Retrofit, usa la IP de tu máquina:
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.2.4:8000/")  // Cambia por la IP de tu PC
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
