package com.example.ia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 101;
    private static final int REQUEST_PERMISSIONS = 100;

    private ApiService apiService;
    private ImageView imageView;
    private Uri imageUri;
    private TextView resultTextView;

    private String[] categories = {
            "Bache",
            "Descascaramiento",
            "Fisura en bloque",
            "Fisura por deslizamiento",
            "Fisura por reflexión",
            "Fisura transversal",
            "Fisuras longitudinales y transversales",
            "Hundimiento",
            "Parche",
            "Pérdida de agregado",
            "Piel de cocodrilo"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular vistas
        MaterialButton btnChooseImage = findViewById(R.id.btnChooseImage);
        MaterialButton btnCaptureImage = findViewById(R.id.btnCaptureImage);
        MaterialButton btnUploadImage = findViewById(R.id.btnUploadImage);
        MaterialButton btnGetLocation = findViewById(R.id.btnGetLocation);
        MaterialButton btnLogout = findViewById(R.id.btnLogout); // Nuevo botón de cerrar sesión

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.iaPredictionTextView);

        // Verificar permisos antes de usar la cámara o galería
        if (!hasPermissions()) {
            requestPermissions();
        }

        // Inicializar Retrofit
        apiService = RetrofitClient.getClient("http://192.168.2.4:8000").create(ApiService.class);

        // Abrir galería para elegir una imagen
        btnChooseImage.setOnClickListener(v -> openGallery());

        // Abrir `CameraActivity` para capturar una imagen
        btnCaptureImage.setOnClickListener(v -> openCameraActivity());

        // Abrir `Maps` para ubicación
        btnGetLocation.setOnClickListener(v -> openMaps());

        // Subir la imagen al servidor
        btnUploadImage.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImage(imageUri);
            } else {
                Toast.makeText(MainActivity.this, "Por favor, selecciona o captura una imagen", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción para cerrar sesión
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InicioL.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Limpia el stack de actividades
            startActivity(intent);
            finish(); // Finaliza la actividad actual
        });
    }

    // Verificar si los permisos están concedidos
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Solicitar permisos en tiempo de ejecución
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS);
    }

    // Método para abrir la galería
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Método para abrir la actividad de la cámara
    private void openCameraActivity() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
    }
    // Método para abrir la actividad de la cámara
    private void openMaps() {
        Intent intent = new Intent(MainActivity.this, maps.class);
        startActivity(intent); // Inicia la actividad de mapas
    }



    // Obtener el resultado de la selección o captura de la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                // Imagen seleccionada desde la galería
                imageUri = data.getData();
                imageView.setImageURI(imageUri); // Mostrar la imagen seleccionada
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data != null) {
                // Imagen capturada desde CameraActivity
                String imageUriString = data.getStringExtra("imageUri");
                if (imageUriString != null) {
                    imageUri = Uri.parse(imageUriString);
                    imageView.setImageURI(imageUri); // Mostrar la imagen capturada
                }
            }
        }
    }

    // Método para subir la imagen al servidor
    private void uploadImage(Uri imageUri) {
        String filePath = FileUtils.getPath(this, imageUri);
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, "Error al obtener la ruta del archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        Call<ResponseBody> call = apiService.uploadImage(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String result = response.body().string();
                        processPredictionResponse(result);
                        Toast.makeText(MainActivity.this, "Imagen subida con éxito", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("Upload Success", "Error al procesar la respuesta: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para procesar la respuesta de la IA y mostrar la predicción
    private void processPredictionResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            if (!jsonObject.has("probabilities")) {
                Toast.makeText(this, "Respuesta inválida del servidor", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONArray predictionsArray = jsonObject.getJSONArray("probabilities").getJSONArray(0);

            double[] probabilities = new double[predictionsArray.length()];
            for (int i = 0; i < predictionsArray.length(); i++) {
                probabilities[i] = predictionsArray.getDouble(i);
            }

            int maxIndex = 0;
            double maxProbability = probabilities[0];
            for (int i = 1; i < probabilities.length; i++) {
                if (probabilities[i] > maxProbability) {
                    maxProbability = probabilities[i];
                    maxIndex = i;
                }
            }

            if (maxIndex < categories.length) {
                String predictedCategory = categories[maxIndex];
             //   resultTextView.setText("Predicción de la IA: " + predictedCategory + " (Probabilidad: " + maxProbability + ")");
                resultTextView.setText( predictedCategory);

            } else {
                resultTextView.setText("Error: No se encontró una categoría para la predicción");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar la predicción", Toast.LENGTH_SHORT).show();
        }
    }
}
