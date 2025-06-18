package com.lksnext.parkingJReboiro.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.parkingJReboiro.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        FirebaseApp.initializeApp(this);
        if (FirebaseApp.getApps(this).isEmpty()) {
            Log.e("FIREBASE", "Firebase NO se ha inicializado correctamente");
        } else {
            Log.i("FIREBASE", "Firebase inicializado correctamente");
        }

        binding.btnRegister.setOnClickListener(v -> {
            if (validarFormulario()) {
                registrarUsuario();
            }
        });

        binding.goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private boolean validarFormulario() {
        boolean esValido = true;

        String email = binding.emailText.getText().toString().trim();
        String password = binding.passwordText.getText().toString();
        String confirmPassword = binding.confirmPasswordText.getText().toString();

        if (binding.usernameText.getText().toString().trim().isEmpty()) {
            binding.usernameLayout.setError("Campo obligatorio");
            esValido = false;
        } else {
            binding.usernameLayout.setError(null);
        }

        if (email.isEmpty()) {
            binding.emailLayout.setError("Campo obligatorio");
            esValido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Email no válido");
            esValido = false;
        } else {
            binding.emailLayout.setError(null);
        }

        if (binding.employeeIdText.getText().toString().trim().isEmpty()) {
            binding.employeeIdLayout.setError("Campo obligatorio");
            esValido = false;
        } else {
            binding.employeeIdLayout.setError(null);
        }

        if (!esPasswordValida(password)) {
            binding.passwordLayout.setError("Mín. 8 caracteres, mayúscula, minúscula y número");
            esValido = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Las contraseñas no coinciden");
            esValido = false;
        } else {
            binding.confirmPasswordLayout.setError(null);
        }

        return esValido;
    }

    private boolean esPasswordValida(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }

    private void registrarUsuario() {
        String email = binding.emailText.getText().toString().trim();
        String password = binding.passwordText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        String error = obtenerMensajeError(task.getException());
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String obtenerMensajeError(Exception e) {
        if (e != null && e.getMessage() != null) {
            if (e.getMessage().contains("email address is already in use")) {
                return "El correo ya está registrado";
            }
            if (e.getMessage().contains("The email address is badly formatted")) {
                return "El formato del correo es incorrecto";
            }
        }
        return "Error en el registro: " + (e != null ? e.getMessage() : "");
    }
}