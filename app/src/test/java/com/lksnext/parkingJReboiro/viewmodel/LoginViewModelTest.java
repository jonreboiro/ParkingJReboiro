package com.lksnext.parkingJReboiro.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.lksnext.parkingJReboiro.data.DataRepository;
import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IDataRepository mockRepository;

    @Mock
    private Observer<Boolean> isLoadingObserver;
    @Mock
    private Observer<Boolean> isLoggedObserver;
    @Mock
    private Observer<String> errorMessageObserver;
    @Mock
    private Observer<String> emailErrorObserver;
    @Mock
    private Observer<String> passwordErrorObserver;
    @Mock
    private Observer<Boolean> needProfileCompletionObserver;

    @Captor
    private ArgumentCaptor<Callback<Void>> loginCallbackCaptor;

    @Captor
    private ArgumentCaptor<Callback<Boolean>> googleCallbackCaptor;

    private LoginViewModel viewModel;

    // Datos de prueba
    private final String validEmail = "juan@test.com";
    private final String validPassword = "abc123";
    private final String invalidPassword = "wrong";

    @Before
    public void setup() {
        viewModel = new LoginViewModel(mockRepository);

        viewModel.isLoading().observeForever(isLoadingObserver);
        viewModel.isLogged().observeForever(isLoggedObserver);
        viewModel.getErrorMessage().observeForever(errorMessageObserver);
        viewModel.getEmailError().observeForever(emailErrorObserver);
        viewModel.getPasswordError().observeForever(passwordErrorObserver);
        viewModel.getNeedProfileCompletion().observeForever(needProfileCompletionObserver);
    }

    @Test
    public void login_success() {
        // Configurar comportamiento del mock
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(2);
            callback.onSuccess(null);
            return null;
        }).when(mockRepository).loginWithEmailAndPassword(eq(validEmail), eq(validPassword), any());

        // Ejecutar
        viewModel.loginUsuario(validEmail, validPassword);

        // Verificar
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.isLogged().getValue(), is(true));
        assertThat(viewModel.getErrorMessage().getValue(), nullValue());
    }

    @Test
    public void login_error() {
        // Configurar comportamiento del mock
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(2);
            callback.onError("Credenciales inválidas");
            return null;
        }).when(mockRepository).loginWithEmailAndPassword(eq(validEmail), eq(invalidPassword), any());

        // Ejecutar
        viewModel.loginUsuario(validEmail, invalidPassword);

        // Verificar
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.isLogged().getValue(), is(false));
        assertThat(viewModel.getErrorMessage().getValue(), containsString("inválidas"));
    }

    @Test
    public void validarFormulario_datosValidos_returnTrue() {
        boolean resultado = viewModel.validarFormulario(validEmail, validPassword);

        assertThat(resultado, is(true));
        assertThat(viewModel.getEmailError().getValue(), nullValue());
        assertThat(viewModel.getPasswordError().getValue(), nullValue());
    }

    @Test
    public void validarFormulario_emailVacio_returnFalse() {
        boolean resultado = viewModel.validarFormulario("", validPassword);

        assertThat(resultado, is(false));
        assertThat(viewModel.getEmailError().getValue(), is("Campo obligatorio"));
    }

    @Test
    public void validarFormulario_emailInvalido_returnFalse() {
        boolean resultado = viewModel.validarFormulario("correo-invalido", validPassword);

        assertThat(resultado, is(false));
        assertThat(viewModel.getEmailError().getValue(), is("Email no válido"));
    }

    @Test
    public void validarFormulario_passwordVacio_returnFalse() {
        boolean resultado = viewModel.validarFormulario(validEmail, "");

        assertThat(resultado, is(false));
        assertThat(viewModel.getPasswordError().getValue(), is("Campo obligatorio"));
    }

    @Test
    public void firebaseAuthWithGoogle_success_noProfileNeeded() {
        String idToken = "google-token";

        // Configurar comportamiento del mock
        doAnswer(invocation -> {
            Callback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(false); // No necesita completar perfil
            return null;
        }).when(mockRepository).loginWithGoogle(eq(idToken), any());

        // Ejecutar
        viewModel.firebaseAuthWithGoogle(idToken);

        // Verificar
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.isLogged().getValue(), is(true));
        assertThat(viewModel.getNeedProfileCompletion().getValue(), nullValue());
    }

    @Test
    public void firebaseAuthWithGoogle_success_profileNeeded() {
        String idToken = "google-token";

        // Configurar comportamiento del mock
        doAnswer(invocation -> {
            Callback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true); // Necesita completar perfil
            return null;
        }).when(mockRepository).loginWithGoogle(eq(idToken), any());

        // Ejecutar
        viewModel.firebaseAuthWithGoogle(idToken);

        // Verificar
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.getNeedProfileCompletion().getValue(), is(true));
    }

    @Test
    public void firebaseAuthWithGoogle_error() {
        String idToken = "google-token";

        // Configurar comportamiento del mock
        doAnswer(invocation -> {
            Callback<Boolean> callback = invocation.getArgument(1);
            callback.onError("Error de autenticación");
            return null;
        }).when(mockRepository).loginWithGoogle(eq(idToken), any());

        // Ejecutar
        viewModel.firebaseAuthWithGoogle(idToken);

        // Verificar
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.isLogged().getValue(), is(false));
        assertThat(viewModel.getErrorMessage().getValue(), is("Error de autenticación"));
    }
}