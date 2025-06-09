package com.lksnext.parkingJReboiro.view.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.databinding.ActivityRecuperarPasswordBinding;

public class RecuperarPasswordActivity extends AppCompatActivity {

    private ActivityRecuperarPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperarPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar botón para enviar solicitud de recuperación
        binding.btnRecuperarPassword.setOnClickListener(v -> {
            String email = binding.emailRecuperarText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí implementarás la lógica para recuperar contraseña con Firebase Auth
                Toast.makeText(this, "Se ha enviado un correo para recuperar tu contraseña", Toast.LENGTH_LONG).show();
            }
        });

        // Configurar el botón para volver a la pantalla de login
        binding.volverLogin.setOnClickListener(v -> finish());
    }
}