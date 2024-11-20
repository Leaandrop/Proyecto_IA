package com.example.ia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

// Actividad para la pantalla de inicio de sesión
public class InicioL extends AppCompatActivity {

    private DatabaseHelper db; // Objeto para interactuar con la base de datos
    private GoogleSignInClient googleSignInClient; // Cliente de inicio de sesión con Google
    private ActivityResultLauncher<Intent> googleSignInLauncher; // Lanzador para manejar el resultado de inicio de sesión con Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciolog); // Asigna el diseño de la actividad

        db = new DatabaseHelper(this); // Inicializa el objeto de la base de datos

        // Configuración para el inicio de sesión con Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // Solicita el correo electrónico del usuario
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso); // Crea un cliente de inicio de sesión con Google

        // Inicializa el lanzador para manejar el resultado de inicio de sesión con Google
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task); // Maneja el resultado del inicio de sesión
                    } else {
                        Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Referencias a los elementos de la interfaz de usuario
        EditText emailInput = findViewById(R.id.entrada_correo); // Campo para ingresar el correo electrónico
        EditText passwordInput = findViewById(R.id.entrada_clave); // Campo para ingresar la contraseña
        Button loginButton = findViewById(R.id.inicio_sesion); // Botón para iniciar sesión
        TextView createAccount = findViewById(R.id.crear_cuenta); // Texto para crear una nueva cuenta
        TextView forgotPassword = findViewById(R.id.olvido); // Texto para recuperar la contraseña
        ImageView logo = findViewById(R.id.logo); // Logo que también permite iniciar sesión con Google
        ImageButton googleButton = findViewById(R.id.google_button); // Botón para iniciar sesión con Google

        // Configura el clic en el logo o el botón de Google para iniciar sesión con Google
        logo.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent); // Lanza el flujo de inicio de sesión con Google
        });
        googleButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Configura el clic en el botón de inicio de sesión
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim(); // Obtiene el correo ingresado
            String password = passwordInput.getText().toString().trim(); // Obtiene la contraseña ingresada

            // Verifica que los campos no estén vacíos
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Comprueba las credenciales del usuario en la base de datos
            boolean valid = db.checkUser(email, password);
            if (valid) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                // Redirige a la actividad principal si las credenciales son válidas
                Intent intent = new Intent(InicioL.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });

        // Configura el clic en "Crear cuenta" para redirigir a la pantalla de registro
        createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        // Configura el clic en "Olvidé mi contraseña" para redirigir a la pantalla de recuperación
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    // Método para manejar el resultado del inicio de sesión con Google
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult();
            if (account != null) {
                String email = account.getEmail(); // Obtiene el correo electrónico del usuario de Google
                Toast.makeText(this, "Inicio de sesión exitoso con Google: " + email, Toast.LENGTH_SHORT).show();

                // Redirige a la actividad principal y pasa el correo electrónico como extra
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            // Maneja errores en el inicio de sesión con Google
            Toast.makeText(this, "Error al iniciar sesión con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
