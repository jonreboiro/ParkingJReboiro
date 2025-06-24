package com.lksnext.parkingJReboiro.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingJReboiro.databinding.FragmentProfileBinding;
import com.lksnext.parkingJReboiro.domain.User;
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

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Configurar observadores para LiveData
        setupObservers();

        // Configurar listeners de botones
        binding.btnSaveProfile.setOnClickListener(v -> guardarCambiosPerfil());
        binding.btnChangePassword.setOnClickListener(v -> mostrarDialogoCambioPassword());
        binding.btnLogout.setOnClickListener(v -> viewModel.logout());
    }

    private void setupObservers() {
        // Observar datos del usuario
        viewModel.getUserData().observe(getViewLifecycleOwner(), this::actualizarUIConDatosUsuario);

        // Observar estado de carga
        viewModel.isLoading().observe(getViewLifecycleOwner(), this::mostrarCargando);

        // Observar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Observar navegación a login
        viewModel.getNavigateToLogin().observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate) {
                navegarALogin();
            }
        });

        // Observar éxito en actualización de perfil
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
            }
        });

        // Observar éxito en cambio de contraseña
        viewModel.getPasswordChangeSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUIConDatosUsuario(User user) {
        if (user != null) {
            binding.usernameText.setText(user.getUsername());
            binding.emailText.setText(user.getEmail());
            binding.phoneText.setText(user.getPhone());
            binding.employeeIdText.setText(user.getEmployeeId());
        }
    }

    private void guardarCambiosPerfil() {
        String username = binding.usernameText.getText().toString().trim();
        String email = binding.emailText.getText().toString().trim();
        String phone = binding.phoneText.getText().toString().trim();
        String employeeId = binding.employeeIdText.getText().toString().trim();

        // Delegar la validación y actualización al ViewModel
        viewModel.updateUserProfile(username, email, phone, employeeId);
    }

    private void mostrarDialogoCambioPassword() {
        View dialogView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, null);
        EditText etPassword = new EditText(getContext());
        etPassword.setHint("Nueva contraseña");
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(getContext())
                .setTitle("Cambiar contraseña")
                .setView(etPassword)
                .setPositiveButton("Cambiar", (dialog, which) -> {
                    String nuevaPassword = etPassword.getText().toString();
                    solicitarPasswordActual(nuevaPassword);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void solicitarPasswordActual(String nuevaPassword) {
        EditText etActual = new EditText(getContext());
        etActual.setHint("Contraseña actual");
        etActual.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(getContext())
                .setTitle("Reautenticación")
                .setMessage("Por seguridad, introduce tu contraseña actual")
                .setView(etActual)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    String passwordActual = etActual.getText().toString();
                    viewModel.changePassword(nuevaPassword, passwordActual);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarCargando(boolean mostrar) {
        binding.btnSaveProfile.setEnabled(!mostrar);
        binding.btnChangePassword.setEnabled(!mostrar);
        binding.btnLogout.setEnabled(!mostrar);
        // Aquí puedes mostrar/ocultar un ProgressBar si lo tienes en el layout
    }

    private void navegarALogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}