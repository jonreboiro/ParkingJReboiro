// app/src/main/java/com/lksnext/parkingplantilla/view/fragment/ProfileFragment.java
package com.lksnext.parkingJReboiro.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingJReboiro.databinding.FragmentProfileBinding;
import com.lksnext.parkingJReboiro.view.activity.LoginActivity;
import com.lksnext.parkingJReboiro.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Cargar datos del usuario
        viewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.usernameText.setText(user.getUsername());
                binding.emailText.setText(user.getEmail());
                binding.phoneText.setText(user.getPhone());
                binding.employeeIdText.setText(user.getEmployeeId());
            }
        });

        // Configurar botón para guardar cambios en el perfil
        binding.btnSaveProfile.setOnClickListener(v -> {
            String username = binding.usernameText.getText().toString();
            String email = binding.emailText.getText().toString();
            String phone = binding.phoneText.getText().toString();
            String employeeId = binding.employeeIdText.getText().toString();

            viewModel.updateUserProfile(username, email, phone, employeeId);
        });

        // Configurar botón para cambiar contraseña
        binding.btnChangePassword.setOnClickListener(v -> {
            // Navegar al fragmento de cambio de contraseña
            // Esto se implementará posteriormente
        });

        // Configurar botón para cerrar sesión
        binding.btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            // Navegar a LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}