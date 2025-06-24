package com.lksnext.parkingJReboiro.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseApp;
import com.lksnext.parkingJReboiro.databinding.ActivityRegisterBinding;
import com.lksnext.parkingJReboiro.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Configurar observadores y listeners
        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        // Observar errores de validación
        viewModel.getUsernameError().observe(this, error ->
                binding.usernameLayout.setError(error));

        viewModel.getEmailError().observe(this, error ->
                binding.emailLayout.setError(error));

        viewModel.getEmployeeIdError().observe(this, error ->
                binding.employeeIdLayout.setError(error));

        viewModel.getPasswordError().observe(this, error ->
                binding.passwordLayout.setError(error));

        viewModel.getConfirmPasswordError().observe(this, error ->
                binding.confirmPasswordLayout.setError(error));

        // Observar estado de registro
        viewModel.isLoading().observe(this, isLoading -> {
            binding.btnRegister.setEnabled(!isLoading);
            // Aquí podrías mostrar un indicador de progreso
        });

        viewModel.getRegistrationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.usernameText.getText().toString().trim();
            String email = binding.emailText.getText().toString().trim();
            String employeeId = binding.employeeIdText.getText().toString().trim();
            String phone = binding.phoneText.getText().toString().trim();
            String password = binding.passwordText.getText().toString();
            String confirmPassword = binding.confirmPasswordText.getText().toString();

            if (viewModel.validarFormulario(username, email, employeeId, password, confirmPassword)) {
                viewModel.registrarUsuario(username, email, employeeId, phone, password);
            }
        });

        binding.goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}