package com.lksnext.parkingJReboiro.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.parkingJReboiro.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(v -> {
            if (validarFormulario()) {
                loginUsuario();
            }
        });

        binding.createAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, RecuperarPasswordActivity.class));
        });
    }

    private boolean validarFormulario() {
        boolean esValido = true;
        String email = binding.emailText.getText().toString().trim();
        String password = binding.passwordText.getText().toString();

        if (email.isEmpty()) {
            binding.email.setError("Campo obligatorio");
            esValido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.setError("Email no válido");
            esValido = false;
        } else {
            binding.email.setError(null);
        }

        if (password.isEmpty()) {
            binding.password.setError("Campo obligatorio");
            esValido = false;
        } else {
            binding.password.setError(null);
        }

        return esValido;
    }

    private void loginUsuario() {
        String email = binding.emailText.getText().toString().trim();
        String password = binding.passwordText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
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
            if (e.getMessage().contains("There is no user record")) {
                return "Usuario no registrado";
            }
            if (e.getMessage().contains("The password is invalid")) {
                return "Contraseña incorrecta";
            }
        }
        return "Error al iniciar sesión: " + (e != null ? e.getMessage() : "");
    }
}