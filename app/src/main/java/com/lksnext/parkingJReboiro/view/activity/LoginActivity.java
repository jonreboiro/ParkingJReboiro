package com.lksnext.parkingJReboiro.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.databinding.ActivityLoginBinding;
import com.lksnext.parkingJReboiro.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar observadores
        setupObservers();

        // Configurar listeners de UI
        setupListeners();
    }

    private void setupObservers() {
        // Observar estado de login
        viewModel.isLogged().observe(this, isLogged -> {
            if (isLogged != null) {
                if (isLogged) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            }
        });

        // Añadir observador para perfil incompleto
        viewModel.getNeedProfileCompletion().observe(this, needsCompletion -> {
            if (needsCompletion != null && needsCompletion) {
                startActivity(new Intent(this, CompleteProfileActivity.class));
                finish();
            }
        });

        // Observar mensajes de error
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        // Observar errores de validación
        viewModel.getEmailError().observe(this, error -> {
            binding.email.setError(error);
        });

        viewModel.getPasswordError().observe(this, error -> {
            binding.password.setError(error);
        });

        // Observar estado de carga
        viewModel.isLoading().observe(this, isLoading -> {
            // Aquí podrías mostrar/ocultar un ProgressBar
        });
    }

    private void setupListeners() {
        binding.googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailText.getText().toString().trim();
            String password = binding.passwordText.getText().toString();
            viewModel.loginUsuario(email, password);
        });

        binding.createAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, RecuperarPasswordActivity.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                viewModel.firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error en Google Sign-In", Toast.LENGTH_SHORT).show();
            }
        }
    }
}