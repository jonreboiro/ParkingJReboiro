package com.lksnext.parkingJReboiro.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.lksnext.parkingJReboiro.viewmodel.CompleteProfileViewModel;
import com.lksnext.parkingJReboiro.databinding.ActivityCompleteProfileBinding;

public class CompleteProfileActivity extends AppCompatActivity {

    private ActivityCompleteProfileBinding binding;
    private CompleteProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompleteProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CompleteProfileViewModel.class);

        setupObservers();

        binding.btnGuardarPerfil.setOnClickListener(v -> guardarPerfil());
    }

    private void setupObservers() {
        viewModel.getProfileSaved().observe(this, isSaved -> {
            if (isSaved != null && isSaved) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarPerfil() {
        String username = binding.etUsername.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String employeeId = binding.etEmployeeId.getText().toString().trim();

        viewModel.saveUserProfile(username, phone, employeeId);
    }
}