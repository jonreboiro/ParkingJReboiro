package com.lksnext.parkingJReboiro.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.lksnext.parkingJReboiro.data.IDataRepository;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompleteProfileViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IDataRepository repository;

    @Mock
    private IFirebaseUser firebaseUser;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<String> errorObserver;

    @Mock
    private Observer<Boolean> profileSavedObserver;

    @Captor
    private ArgumentCaptor<Callback<Void>> callbackCaptor;

    private CompleteProfileViewModel viewModel;

    private static final String USERNAME = "Pepe";
    private static final String EMAIL = "pepe@mail.com";
    private static final String PHONE = "600123456";
    private static final String EMPLOYEE_ID = "EMP001";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(repository.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getEmail()).thenReturn(EMAIL);

        viewModel = new CompleteProfileViewModel(repository);

        // Configuramos observadores para LiveData
        viewModel.isLoading().observeForever(loadingObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);
        viewModel.getProfileSaved().observeForever(profileSavedObserver);
    }

    @Test
    public void saveUserProfile_success() {
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(1);
            callback.onSuccess(null);
            return null;
        }).when(repository).updateUserProfile(any(User.class), any(Callback.class));

        viewModel.saveUserProfile(USERNAME, PHONE, EMPLOYEE_ID);

        verify(loadingObserver).onChanged(true);
        verify(profileSavedObserver).onChanged(true);
        // Usar atLeastOnce() en lugar de verificar exactamente una vez
        verify(loadingObserver, atLeastOnce()).onChanged(false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).updateUserProfile(userCaptor.capture(), any());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername(), equalTo(USERNAME));
        assertThat(capturedUser.getEmail(), equalTo(EMAIL));
        assertThat(capturedUser.getPhone(), equalTo(PHONE));
        assertThat(capturedUser.getEmployeeId(), equalTo(EMPLOYEE_ID));
    }

    @Test
    public void saveUserProfile_error() {
        String errorMsg = "Network error";
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(1);
            callback.onError(errorMsg);
            return null;
        }).when(repository).updateUserProfile(any(User.class), any(Callback.class));

        viewModel.saveUserProfile(USERNAME, PHONE, EMPLOYEE_ID);

        verify(loadingObserver).onChanged(true);
        verify(errorObserver).onChanged(errorMsg);
        // Usar atLeastOnce() en lugar de verificar exactamente una vez
        verify(loadingObserver, atLeastOnce()).onChanged(false);
        verify(profileSavedObserver, never()).onChanged(true);
    }

    @Test
    public void saveUserProfile_emptyUsername() {
        // Ejecutar solo con nombre vacío
        viewModel.saveUserProfile("", PHONE, EMPLOYEE_ID);

        // Verificar
        verify(errorObserver).onChanged(any());
        verify(repository, never()).updateUserProfile(any(), any());
    }

    @Test
    public void saveUserProfile_emptyEmployeeId() {
        // Ejecutar solo con ID de empleado vacío
        viewModel.saveUserProfile(USERNAME, PHONE, "");

        // Verificar
        verify(errorObserver).onChanged(any());
        verify(repository, never()).updateUserProfile(any(), any());
    }

    @Test
    public void saveUserProfile_noAuthenticatedUser() {
        when(repository.getCurrentUser()).thenReturn(null);

        viewModel.saveUserProfile(USERNAME, PHONE, EMPLOYEE_ID);

        verify(errorObserver).onChanged("No hay usuario autenticado");
        verify(repository, never()).updateUserProfile(any(), any());
    }
}