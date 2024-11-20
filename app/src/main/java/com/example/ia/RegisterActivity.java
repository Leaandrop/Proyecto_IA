package com.example.ia;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        EditText nameInput = findViewById(R.id.registro_nombre);
        EditText surnameInput = findViewById(R.id.registro_apellido);
        EditText emailInput = findViewById(R.id.registro_email);
        EditText passwordInput = findViewById(R.id.registro_clave);
        EditText phoneInput = findViewById(R.id.registro_celular);
        Button registerButton = findViewById(R.id.registro_boton);

        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String surname = surnameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = db.registerUser(name, surname, email, password, phone);
                if (success) {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button backButton = findViewById(R.id.boton_regreso);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, InicioL.class);
            startActivity(intent);
            finish();
        });
    }
}
