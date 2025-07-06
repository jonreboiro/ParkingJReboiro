package com.lksnext.parkingJReboiro.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.data.IFirebaseAuthProvider;
import com.lksnext.parkingJReboiro.data.IFirebaseUser;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProfileViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IDataRepository repository;

    @Mock
    private IFirebaseUser firebaseUser;

    @Mock
    private IFirebaseAuthProvider authProvider;

    @Mock
    private Observer<User> userDataObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<String> errorObserver;

    @Mock
    private Observer<Boolean> updateSuccessObserver;

    @Mock
    private Observer<List<String>> matriculasObserver;

    @Captor
    private ArgumentCaptor<Callback<List<String>>> matriculasCallbackCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private ProfileViewModel viewModel;
    private User testUser;
    private final List<String> MATRICULAS = Arrays.asList("1234BBC", "5678DJF");

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar usuario de prueba
        testUser = new User();
        testUser.setUsername("Usuario Test");
        testUser.setEmail("test@example.com");
        testUser.setPhone("600123456");
        testUser.setEmployeeId("EMP001");
        testUser.setMatriculas(new ArrayList<>(MATRICULAS));

        // Configurar mocks
        when(repository.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getEmail()).thenReturn("test@example.com");
        when(firebaseUser.getUid()).thenReturn("uid_test");

        when(authProvider.getCurrentUser()).thenReturn(firebaseUser);
        when(authProvider.getUid()).thenReturn("uid_test");

        // Configurar LiveData para retornar el usuario de prueba
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        userLiveData.setValue(testUser);
        when(repository.getUserProfile(anyString())).thenReturn(userLiveData);

        // Inicializar ViewModel
        viewModel = new ProfileViewModel(repository, authProvider);

        // Configurar observadores
        viewModel.getUserData().observeForever(userDataObserver);
        viewModel.isLoading().observeForever(loadingObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);
        viewModel.getUpdateSuccess().observeForever(updateSuccessObserver);
        viewModel.getMatriculasList().observeForever(matriculasObserver);
    }

    @Test
    public void loadUserData_success() {
        reset(userDataObserver);
        viewModel.loadUserData();

        verify(userDataObserver).onChanged(any(User.class));
    }

    @Test
    public void loadPlates_success() {
        reset(matriculasObserver);
        viewModel.loadUserData();

        // Verificar que las matrículas se cargaron correctamente
        verify(matriculasObserver).onChanged(MATRICULAS);
        assertThat(viewModel.getMatriculasList().getValue(), hasItems("1234BBC", "5678DJF"));
    }

    @Test
    public void addPlate_success() {
        reset(loadingObserver, matriculasObserver, errorObserver);
        String newPlate = "1234BCD";
        List<String> updatedList = new ArrayList<>(MATRICULAS);
        updatedList.add(newPlate);

        doAnswer(invocation -> {
            Callback<List<String>> callback = invocation.getArgument(1);
            callback.onSuccess(updatedList);
            return null;
        }).when(repository).addMatricula(anyString(), any(Callback.class));

        viewModel.addPlate(newPlate);

        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(matriculasObserver).onChanged(updatedList);
    }

    @Test
    public void addPlate_duplicate() {
        reset(loadingObserver);
        String existingPlate = "1234BBC";

        doAnswer(invocation -> {
            Callback<List<String>> callback = invocation.getArgument(1);
            callback.onError("La matrícula ya existe");
            return null;
        }).when(repository).addMatricula(anyString(), any(Callback.class));

        viewModel.addPlate(existingPlate);

        verify(loadingObserver).onChanged(true);
        verify(errorObserver).onChanged("La matrícula ya existe");
        verify(loadingObserver).onChanged(false);
    }

    @Test
    public void addPlate_invalidFormat() {
        // Matrícula inválida (contiene vocal)
        viewModel.addPlate("1234ABC");

        verify(errorObserver).onChanged("Formato de matrícula inválido. Debe ser 4 números seguidos de 3 consonantes mayúsculas.");
        verify(repository, never()).addMatricula(anyString(), any());
    }

    @Test
    public void removePlate_success() {
        reset(loadingObserver, matriculasObserver, errorObserver);
        String plateToRemove = "5678DJF";
        List<String> updatedList = new ArrayList<>(MATRICULAS);
        updatedList.remove(plateToRemove);

        doAnswer(invocation -> {
            Callback<List<String>> callback = invocation.getArgument(1);
            callback.onSuccess(updatedList);
            return null;
        }).when(repository).removeMatricula(anyString(), any(Callback.class));

        viewModel.removePlate(plateToRemove);

        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(matriculasObserver).onChanged(updatedList);
        assertThat(viewModel.getMatriculasList().getValue(), not(hasItem("5678DJF")));
    }

    @Test
    public void removePlate_error() {
        reset(loadingObserver, errorObserver);
        String plateToRemove = "5678DJF";
        String errorMessage = "Error al eliminar la matrícula";

        doAnswer(invocation -> {
            Callback<List<String>> callback = invocation.getArgument(1);
            callback.onError(errorMessage);
            return null;
        }).when(repository).removeMatricula(anyString(), any(Callback.class));

        viewModel.removePlate(plateToRemove);

        verify(loadingObserver).onChanged(true);
        verify(errorObserver).onChanged("Error al eliminar la matrícula: " + errorMessage);
        verify(loadingObserver).onChanged(false);
    }

    @Test
    public void updateUserProfile_success() {
        reset(loadingObserver, updateSuccessObserver, errorObserver);
        String username = "Nuevo Nombre";
        String email = "test@example.com";
        String phone = "600111222";
        String employeeId = "EMP002";

        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(1);
            callback.onSuccess(null);
            return null;
        }).when(repository).updateUserProfile(any(User.class), any(Callback.class));

        viewModel.updateUserProfile(username, email, phone, employeeId);

        verify(loadingObserver).onChanged(true);
        verify(updateSuccessObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);

        verify(repository).updateUserProfile(userCaptor.capture(), any());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername(), equalTo(username));
        assertThat(capturedUser.getPhone(), equalTo(phone));
        assertThat(capturedUser.getEmployeeId(), equalTo(employeeId));
    }

    @Test
    public void updateUserProfile_emptyUsername() {
        viewModel.updateUserProfile("", "test@example.com", "600123456", "EMP001");

        verify(errorObserver).onChanged("El nombre no puede estar vacío");
        verify(repository, never()).updateUserProfile(any(), any());
    }

    @Test
    public void updateUserProfile_invalidPhone() {
        viewModel.updateUserProfile("Usuario", "test@example.com", "abc", "EMP001");

        verify(errorObserver).onChanged("Teléfono no válido");
        verify(repository, never()).updateUserProfile(any(), any());
    }

    @Test
    public void changePassword_success() {
        reset(loadingObserver);
        String newPassword = "Nuevo123";
        String currentPassword = "Viejo123";

        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(2);
            callback.onSuccess(null);
            return null;
        }).when(repository).changePassword(anyString(), anyString(), any(Callback.class));

        viewModel.changePassword(newPassword, currentPassword);

        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
    }

    @Test
    public void changePassword_invalid() {
        viewModel.changePassword("weak", "Viejo123");

        verify(errorObserver).onChanged("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número");
        verify(repository, never()).changePassword(anyString(), anyString(), any());
    }

    @Test
    public void validateMatricula() {
        // Matrículas válidas
        assertThat(viewModel.esMatriculaEspanolaValida("1234BCD"), equalTo(true));
        assertThat(viewModel.esMatriculaEspanolaValida("5678DJF"), equalTo(true));

        // Matrículas inválidas
        assertThat(viewModel.esMatriculaEspanolaValida("1234ABC"), equalTo(false)); // Contiene vocal
        assertThat(viewModel.esMatriculaEspanolaValida("123BCDF"), equalTo(false)); // Formato incorrecto
        assertThat(viewModel.esMatriculaEspanolaValida("ABCD123"), equalTo(false)); // Orden incorrecto
    }

    @Test
    public void logout_success() {
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(0);
            callback.onSuccess(null);
            return null;
        }).when(repository).signOut(any(Callback.class));

        viewModel.logout();

        verify(repository).signOut(any(Callback.class));
    }
}