package com.lksnext.parkingJReboiro.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.adapter.MatriculasAdapter;
import com.lksnext.parkingJReboiro.databinding.FragmentProfileBinding;
import com.lksnext.parkingJReboiro.domain.User;
import com.lksnext.parkingJReboiro.view.activity.LoginActivity;
import com.lksnext.parkingJReboiro.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    private MatriculasAdapter matriculasAdapter;
    private boolean isEditMode = false;


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

        setupRecyclerView();

        setupObservers();

        // Configurar listeners de botones
        binding.btnEditProfile.setOnClickListener(v -> toggleEditMode());
        binding.btnSaveProfile.setOnClickListener(v -> guardarCambiosPerfil());
        binding.btnChangePassword.setOnClickListener(v -> mostrarDialogoCambioPassword());
        binding.btnLogout.setOnClickListener(v -> viewModel.logout());
        binding.btnAddMatricula.setOnClickListener(v -> mostrarDialogoNuevaMatricula());
    }

    private void setupRecyclerView() {
        matriculasAdapter = new MatriculasAdapter(matricula -> viewModel.removePlate(matricula));
        binding.recyclerMatriculas.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerMatriculas.setAdapter(matriculasAdapter);
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
                if (isEditMode) {
                    toggleEditMode();
                    viewModel.loadUserData();
                }
            }
        });

        // Observar éxito en cambio de contraseña
        viewModel.getPasswordChangeSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getMatriculasList().observe(getViewLifecycleOwner(), matriculas -> {
            matriculasAdapter.setMatriculas(matriculas);
            binding.tvEmptyMatriculas.setVisibility(
                    matriculas == null || matriculas.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;

        // Activar/desactivar campos de edición
        binding.usernameTil.setEnabled(isEditMode);
        binding.emailTil.setEnabled(isEditMode);
        binding.phoneTil.setEnabled(isEditMode);

        // ID de empleado siempre es de solo lectura
        binding.employeeIdTil.setEnabled(false);

        // Mostrar/ocultar botón de guardar
        binding.btnSaveProfile.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        // Cambiar texto del botón editar
        binding.btnEditProfile.setText(isEditMode ? "Cancelar" : "Editar");
        binding.btnEditProfile.setIcon(getResources().getDrawable(
                isEditMode ? android.R.drawable.ic_menu_close_clear_cancel :
                        android.R.drawable.ic_menu_edit));

        // Si cancelamos, restaurar datos originales
        if (!isEditMode) {
            User currentUser = viewModel.getUserData().getValue();
            if (currentUser != null) {
                actualizarUIConDatosUsuario(currentUser);
            }
        }
    }

    private void mostrarDialogoNuevaMatricula() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nueva matrícula");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_matricula, null);
        EditText input = dialogView.findViewById(R.id.editTextMatricula);
        input.setFilters(new InputFilter[] { new InputFilter.AllCaps() });

        builder.setView(dialogView);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String matricula = input.getText().toString().trim();
            if (!matricula.isEmpty()) {
                viewModel.addPlate(matricula);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Mostrar teclado automáticamente
        input.requestFocus();
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