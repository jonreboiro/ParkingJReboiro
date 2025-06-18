package com.lksnext.parkingJReboiro.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        binding.loginButton.setOnClickListener(v -> {
            if (validarFormulario()) {
                loginUsuario();
            }
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
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error en Google Sign-In", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Error autenticando con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validarFormulario() {
        boolean esValido = true;
        String email = binding.emailText.getText().toString().trim();
        String password = binding.passwordText.getText().toString();

        if (email.isEmpty()) {
            binding.email.setError("Campo obligatorio");
            esValido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.setError("Email no válido");
            esValido = false;
        } else {
            binding.email.setError(null);
        }

        if (password.isEmpty()) {
            binding.password.setError("Campo obligatorio");
            esValido = false;
        } else {
            binding.password.setError(null);
        }

        return esValido;
    }

    private void loginUsuario() {
        String email = binding.emailText.getText().toString().trim();
        String password = binding.passwordText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        String error = obtenerMensajeError(task.getException());
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String obtenerMensajeError(Exception e) {
        if (e != null && e.getMessage() != null) {
            if (e.getMessage().contains("There is no user record")) {
                return "Usuario no registrado";
            }
            if (e.getMessage().contains("The password is invalid")) {
                return "Contraseña incorrecta";
            }
        }
        return "Error al iniciar sesión: " + (e != null ? e.getMessage() : "");
    }
}