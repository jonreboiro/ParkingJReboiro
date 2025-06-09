package com.lksnext.parkingJReboiro.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingJReboiro.databinding.ActivityRegisterBinding;
import com.lksnext.parkingJReboiro.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asignamos la vista/interfaz de registro
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Asignamos el viewModel de register
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Configurar botón de registro
        binding.btnRegister.setOnClickListener(v -> {
            if (validarFormulario()) {
                // Aquí iría la lógica de registro con el ViewModel
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                // Volver a la pantalla de login
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Configurar navegación a login
        binding.goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validarFormulario() {
        boolean esValido = true;

        // Validar campos obligatorios
        if (binding.usernameText.getText().toString().trim().isEmpty()) {
            binding.usernameLayout.setError("Campo obligatorio");
            esValido = false;
        } else {
            binding.usernameLayout.setError(null);
        }

        if (binding.emailText.getText().toString().trim().isEmpty()) {
            binding.emailLayout.setError("Campo obligatorio");
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

        if (binding.passwordText.getText().toString().trim().isEmpty()) {
            binding.passwordLayout.setError("Campo obligatorio");
            esValido = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        // Validar que las contraseñas coincidan
        String password = binding.passwordText.getText().toString();
        String confirmPassword = binding.confirmPasswordText.getText().toString();

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Las contraseñas no coinciden");
            esValido = false;
        } else {
            binding.confirmPasswordLayout.setError(null);
        }

        return esValido;
    }
}