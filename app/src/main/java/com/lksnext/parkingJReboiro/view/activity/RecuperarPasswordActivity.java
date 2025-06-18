package com.lksnext.parkingJReboiro.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.parkingJReboiro.databinding.ActivityRecuperarPasswordBinding;

public class RecuperarPasswordActivity extends AppCompatActivity {

    private ActivityRecuperarPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperarPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        binding.btnRecuperarPassword.setOnClickListener(v -> {
            String email = binding.emailRecuperarText.getText().toString().trim();
            if (!esEmailValido(email)) {
                binding.emailRecuperar.setError("Introduce un correo válido");
                return;
            } else {
                binding.emailRecuperar.setError(null);
            }
            mostrarCargando(true);
            enviarCorreoRecuperacion(email);
        });

        binding.volverLogin.setOnClickListener(v -> finish());
    }

    private boolean esEmailValido(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void mostrarCargando(boolean mostrar) {
        // Puedes agregar un ProgressBar en el layout y mostrarlo/ocultarlo aquí
        // Ejemplo: binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        // Si no tienes ProgressBar, puedes deshabilitar el botón mientras tanto:
        binding.btnRecuperarPassword.setEnabled(!mostrar);
    }

    private void enviarCorreoRecuperacion(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    mostrarCargando(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Si el correo está registrado, recibirás un email para restablecer tu contraseña.", Toast.LENGTH_LONG).show();
                        // Opcional: redirigir a LoginActivity
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        String mensaje = "No se pudo enviar el correo de recuperación. Intenta de nuevo.";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            String error = task.getException().getMessage();
                            if (error.contains("badly formatted")) {
                                mensaje = "El correo no tiene un formato válido.";
                            }
                        }
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                    }
                });
    }
}