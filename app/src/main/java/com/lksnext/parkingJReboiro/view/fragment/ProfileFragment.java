// app/src/main/java/com/lksnext/parkingJReboiro/view/fragment/ProfileFragment.java
package com.lksnext.parkingJReboiro.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.parkingJReboiro.databinding.FragmentProfileBinding;
import com.lksnext.parkingJReboiro.view.activity.LoginActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressBar = new ProgressBar(getContext());
        // Puedes agregar el ProgressBar al layout si lo deseas

        if (mAuth.getCurrentUser() == null) {
            navegarALogin();
            return;
        }

        cargarDatosUsuario();

        binding.btnSaveProfile.setOnClickListener(v -> guardarCambiosPerfil());
        binding.btnChangePassword.setOnClickListener(v -> mostrarDialogoCambioPassword());
        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            navegarALogin();
        });
    }

    private void cargarDatosUsuario() {
        mostrarCargando(true);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navegarALogin();
            return;
        }
        String uid = user.getUid();
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                binding.usernameText.setText(documentSnapshot.getString("username"));
                binding.emailText.setText(documentSnapshot.getString("email"));
                binding.phoneText.setText(documentSnapshot.getString("phone"));
                binding.employeeIdText.setText(documentSnapshot.getString("employeeId"));
            }
            mostrarCargando(false);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
            mostrarCargando(false);
        });
    }

    private void guardarCambiosPerfil() {
        String username = binding.usernameText.getText().toString().trim();
        String email = binding.emailText.getText().toString().trim();
        String phone = binding.phoneText.getText().toString().trim();
        String employeeId = binding.employeeIdText.getText().toString().trim();

        if (username.isEmpty()) {
            binding.usernameTil.setError("El nombre no puede estar vacío");
            return;
        } else {
            binding.usernameTil.setError(null);
        }
        if (!Pattern.matches("^\\+?\\d{9,15}$", phone)) {
            binding.phoneTil.setError("Teléfono no válido");
            return;
        } else {
            binding.phoneTil.setError(null);
        }

        mostrarCargando(true);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navegarALogin();
            return;
        }
        String uid = user.getUid();
        Map<String, Object> datos = new HashMap<>();
        datos.put("username", username);
        datos.put("email", email);
        datos.put("phone", phone);
        datos.put("employeeId", employeeId);

        db.collection("users").document(uid).set(datos)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    mostrarCargando(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar cambios", Toast.LENGTH_SHORT).show();
                    mostrarCargando(false);
                });
    }

    private void mostrarDialogoCambioPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "Necesitas volver a iniciar sesión para cambiar la contraseña", Toast.LENGTH_LONG).show();
            navegarALogin();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, null);
        EditText etPassword = new EditText(getContext());
        etPassword.setHint("Nueva contraseña");
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(getContext())
                .setTitle("Cambiar contraseña")
                .setView(etPassword)
                .setPositiveButton("Cambiar", (dialog, which) -> {
                    String nuevaPassword = etPassword.getText().toString();
                    if (!esPasswordValida(nuevaPassword)) {
                        Toast.makeText(getContext(), "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número", Toast.LENGTH_LONG).show();
                        return;
                    }
                    reautenticarYActualizarPassword(user, nuevaPassword);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void reautenticarYActualizarPassword(FirebaseUser user, String nuevaPassword) {
        // Pide la contraseña actual para reautenticación
        EditText etActual = new EditText(getContext());
        etActual.setHint("Contraseña actual");
        etActual.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(getContext())
                .setTitle("Reautenticación")
                .setMessage("Por seguridad, introduce tu contraseña actual")
                .setView(etActual)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    String actual = etActual.getText().toString();
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), actual);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    user.updatePassword(nuevaPassword)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getContext(), "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(getContext(), "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean esPasswordValida(String password) {
        return password.length() >= 8 &&
                Pattern.compile("[A-Z]").matcher(password).find() &&
                Pattern.compile("[a-z]").matcher(password).find() &&
                Pattern.compile("[0-9]").matcher(password).find();
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