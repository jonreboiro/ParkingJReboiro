package com.lksnext.parkingJReboiro.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class RegisterViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IDataRepository mockRepository;

    @Mock
    private Observer<String> usernameErrorObserver;
    @Mock
    private Observer<String> emailErrorObserver;
    @Mock
    private Observer<String> employeeIdErrorObserver;
    @Mock
    private Observer<String> passwordErrorObserver;
    @Mock
    private Observer<String> confirmPasswordErrorObserver;
    @Mock
    private Observer<Boolean> isLoadingObserver;
    @Mock
    private Observer<Boolean> registrationSuccessObserver;
    @Mock
    private Observer<String> errorMessageObserver;

    @Captor
    private ArgumentCaptor<Callback<Void>> registerCallbackCaptor;

    private RegisterViewModel viewModel;

    // Datos de prueba
    private final String validUsername = "Carlos";
    private final String validEmail = "carlos@mail.com";
    private final String validEmployeeId = "EMP002";
    private final String validPhone = "123456789";
    private final String validPassword = "Pwd12345";

    @Before
    public void setup() {
        // Crear el ViewModel con el repositorio mockeado
        viewModel = new RegisterViewModel(mockRepository);

        // Observar todos los LiveData
        viewModel.getUsernameError().observeForever(usernameErrorObserver);
        viewModel.getEmailError().observeForever(emailErrorObserver);
        viewModel.getEmployeeIdError().observeForever(employeeIdErrorObserver);
        viewModel.getPasswordError().observeForever(passwordErrorObserver);
        viewModel.getConfirmPasswordError().observeForever(confirmPasswordErrorObserver);
        viewModel.isLoading().observeForever(isLoadingObserver);
        viewModel.getRegistrationSuccess().observeForever(registrationSuccessObserver);
        viewModel.getErrorMessage().observeForever(errorMessageObserver);
    }

    @Test
    public void register_success() {
        // Configurar comportamiento del mock
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(5);
            callback.onSuccess(null);
            return null;
        }).when(mockRepository).registerUser(
                eq(validUsername),
                eq(validEmail),
                eq(validEmployeeId),
                eq(validPhone),
                eq(validPassword),
                any());

        // Ejecutar
        viewModel.registrarUsuario(validUsername, validEmail, validEmployeeId, validPhone, validPassword);

        // Verificar
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.getRegistrationSuccess().getValue(), is(true));
        assertThat(viewModel.getErrorMessage().getValue(), nullValue());
    }

    @Test
    public void register_error() {
        // Configurar comportamiento del mock
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(5);
            callback.onError("El email ya está en uso");
            return null;
        }).when(mockRepository).registerUser(
                eq(validUsername),
                eq(validEmail),
                eq(validEmployeeId),
                eq(validPhone),
                eq(validPassword),
                any());

        // Ejecutar
        viewModel.registrarUsuario(validUsername, validEmail, validEmployeeId, validPhone, validPassword);

        // Verificar
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.getRegistrationSuccess().getValue(), nullValue());
        assertThat(viewModel.getErrorMessage().getValue(), containsString("uso"));
    }

    @Test
    public void validarFormulario_datosValidos_returnTrue() {
        boolean resultado = viewModel.validarFormulario(
                validUsername, validEmail, validEmployeeId, validPassword, validPassword);

        assertThat(resultado, is(true));
        assertThat(viewModel.getUsernameError().getValue(), nullValue());
        assertThat(viewModel.getEmailError().getValue(), nullValue());
        assertThat(viewModel.getEmployeeIdError().getValue(), nullValue());
        assertThat(viewModel.getPasswordError().getValue(), nullValue());
        assertThat(viewModel.getConfirmPasswordError().getValue(), nullValue());
    }

    @Test
    public void validarFormulario_usernameVacio_returnFalse() {
        boolean resultado = viewModel.validarFormulario(
                "", validEmail, validEmployeeId, validPassword, validPassword);

        assertThat(resultado, is(false));
        assertThat(viewModel.getUsernameError().getValue(), is("Campo obligatorio"));
    }

    @Test
    public void validarFormulario_emailVacio_returnFalse() {
        boolean resultado = viewModel.validarFormulario(
                validUsername, "", validEmployeeId, validPassword, validPassword);

        assertThat(resultado, is(false));
        assertThat(viewModel.getEmailError().getValue(), is("Campo obligatorio"));
    }

    @Test
    public void validarFormulario_emailInvalido_returnFalse() {
        boolean resultado = viewModel.validarFormulario(
                validUsername, "correo-invalido", validEmployeeId, validPassword, validPassword);

        assertThat(resultado, is(false));
        assertThat(viewModel.getEmailError().getValue(), is("Email no válido"));
    }

    @Test
    public void validarFormulario_employeeIdVacio_returnFalse() {
        boolean resultado = viewModel.validarFormulario(
                validUsername, validEmail, "", validPassword, validPassword);

        assertThat(resultado, is(false));
        assertThat(viewModel.getEmployeeIdError().getValue(), is("Campo obligatorio"));
    }

    @Test
    public void validarFormulario_passwordInvalida_returnFalse() {
        boolean resultado = viewModel.validarFormulario(
                validUsername, validEmail, validEmployeeId, "abc123", "abc123");

        assertThat(resultado, is(false));
        assertThat(viewModel.getPasswordError().getValue(),
                containsString("Mín. 8 caracteres, mayúscula, minúscula y número"));
    }

    @Test
    public void validarFormulario_passwordsNoCoinciden_returnFalse() {
        boolean resultado = viewModel.validarFormulario(
                validUsername, validEmail, validEmployeeId, validPassword, "OtraPassword1");

        assertThat(resultado, is(false));
        assertThat(viewModel.getConfirmPasswordError().getValue(), is("Las contraseñas no coinciden"));
    }
}