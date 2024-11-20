package com.example.ia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Actividad para gestionar la funcionalidad de "Olvidé mi contraseña"
public class ForgotPasswordActivity extends AppCompatActivity {

    private DatabaseHelper db; // Objeto para interactuar con la base de datos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password); // Asigna el diseño de la actividad

        db = new DatabaseHelper(this); // Inicializa el objeto de la base de datos

        // Referencias a los elementos de la interfaz de usuario
        EditText emailInput = findViewById(R.id.forgot_email); // Campo de entrada para el email
        EditText newPasswordInput = findViewById(R.id.forgot_new_password); // Campo de entrada para la nueva contraseña
        Button resetButton = findViewById(R.id.forgot_boton); // Botón para restablecer la contraseña
        Button backButton = findViewById(R.id.boton_regreso); // Botón para volver a la pantalla anterior

        // Configura el comportamiento del botón "Restablecer contraseña"
        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim(); // Obtiene el email ingresado
            String newPassword = newPasswordInput.getText().toString().trim(); // Obtiene la nueva contraseña ingresada

            // Verifica que los campos no estén vacíos
            if (email.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Intenta actualizar la contraseña en la base de datos
            boolean updated = db.updatePassword(email, newPassword);
            if (updated) {
                // Muestra un mensaje si la contraseña fue actualizada
                Toast.makeText(this, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad actual
            } else {
                // Muestra un mensaje si el correo no está registrado
                Toast.makeText(this, "El correo no está registrado", Toast.LENGTH_SHORT).show();
            }
        });

        // Configura el comportamiento del botón "Volver"
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, InicioL.class); // Crea un intento para ir a la pantalla de inicio
            startActivity(intent); // Inicia la nueva actividad
            finish(); // Cierra la actividad actual
        });
    }
}
