package com.lksnext.parkingJReboiro.viewmodel;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;

import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.data.IFirebaseUser;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.Hora;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.domain.ReservaConTiempo;
import com.lksnext.parkingJReboiro.notifications.NotificationScheduler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 31)
public class ReservasViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IDataRepository repository;

    @Mock
    private Observer<List<Reserva>> reservasProximasObserver;

    @Mock
    private Observer<List<Reserva>> reservasPasadasObserver;

    @Mock
    private Observer<List<ReservaConTiempo>> reservasActivasConTiempoObserver;

    @Mock
    private Observer<String> errorMessageObserver;

    @Mock
    private Observer<Boolean> isLoadingObserver;

    @Mock
    private Observer<Boolean> operacionExitosaObserver;

    @Captor
    private ArgumentCaptor<List<Reserva>> reservasCaptor;

    @Captor
    private ArgumentCaptor<List<ReservaConTiempo>> reservasConTiempoCaptor;

    private ReservasViewModel viewModel;
    private Context mockContext;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        viewModel = new ReservasViewModel(repository);

        viewModel.getReservasProximas().observeForever(reservasProximasObserver);
        viewModel.getReservasPasadas().observeForever(reservasPasadasObserver);
        viewModel.getReservasActivasConTiempo().observeForever(reservasActivasConTiempoObserver);
        viewModel.getErrorMessage().observeForever(errorMessageObserver);
        viewModel.isLoading().observeForever(isLoadingObserver);
        viewModel.getOperacionExitosa().observeForever(operacionExitosaObserver);

        mockContext = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void cargarReservasUsuario_success() {
        // Crear reservas de prueba
        List<Reserva> mockReservas = new ArrayList<>();
        Reserva reserva = new Reserva();
        reserva.setId("1");
        reserva.setPlazaId(new Plaza(7L, "normal"));
        reserva.setFecha("07/07/2025");
        reserva.setMatricula("7890HJK");
        Hora hora = new Hora(10 * 60 * 60 * 1000, 12 * 60 * 60 * 1000);
        reserva.setHoraInicio(hora);
        mockReservas.add(reserva);

        // Crear mapa de reservas clasificadas
        Map<String, List<Reserva>> reservasClasificadas = new HashMap<>();
        reservasClasificadas.put("proximas", mockReservas);
        reservasClasificadas.put("pasadas", new ArrayList<>());
        reservasClasificadas.put("actual", new ArrayList<>());

        // Simular usuario actual
        when(repository.getCurrentUser()).thenReturn(mock(IFirebaseUser.class));
        // Simular respuesta del repositorio
        doAnswer(invocation -> {
            Callback<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(mockReservas);
            return null;
        }).when(repository).getReservasUsuarioActual(any());

        when(repository.clasificarReservas(mockReservas)).thenReturn(reservasClasificadas);

        viewModel.cargarReservasUsuario();

        // Verificaciones
        verify(isLoadingObserver).onChanged(true);
        verify(isLoadingObserver, atLeastOnce()).onChanged(false);
        verify(reservasProximasObserver, atLeastOnce()).onChanged(reservasCaptor.capture());

        // Validar resultados
        List<Reserva> capturedReservas = reservasCaptor.getValue();
        assertThat(capturedReservas, hasSize(1));
        assertThat(capturedReservas.get(0).getPlazaId().getId(), equalTo(7L));
        assertThat(capturedReservas.get(0).getMatricula(), equalTo("7890HJK"));
    }

    @Test
    public void cargarReservasUsuario_error() {
        // Simular usuario actual
        when(repository.getCurrentUser()).thenReturn(mock(IFirebaseUser.class));
        // Simular error del repositorio
        String errorMsg = "Error de conexión";
        doAnswer(invocation -> {
            Callback<List<Reserva>> callback = invocation.getArgument(0);
            callback.onError(errorMsg);
            return null;
        }).when(repository).getReservasUsuarioActual(any());

        viewModel.cargarReservasUsuario();

        // Verificaciones
        verify(isLoadingObserver).onChanged(true);
        verify(isLoadingObserver, atLeastOnce()).onChanged(false);
        verify(errorMessageObserver).onChanged("Error al cargar reservas: " + errorMsg);
    }

    @Test
    public void cargarReservasUsuario_sinUsuario() {
        // Simular que no hay usuario actual
        when(repository.getCurrentUser()).thenReturn(null);

        viewModel.cargarReservasUsuario();

        // Verificaciones
        verify(isLoadingObserver).onChanged(true);
        verify(isLoadingObserver, atLeastOnce()).onChanged(false);
        verify(errorMessageObserver).onChanged("Debes iniciar sesión para ver tus reservas");
    }

    @Test
    public void cancelarReserva_success() {
        try (MockedStatic<NotificationScheduler> mocked = org.mockito.Mockito.mockStatic(NotificationScheduler.class)) {
            mocked.when(() -> NotificationScheduler.showInstantNotification(
                    any(Context.class), any(Integer.class), any(String.class), any(String.class)
            )).thenAnswer(invocation -> null);
            // Crear reserva de prueba y agregarla a una lista
            Reserva reserva = new Reserva();
            reserva.setId("1");
            reserva.setPlazaId(new Plaza(7L, "normal"));

            List<Reserva> reservasProximas = new ArrayList<>();
            reservasProximas.add(reserva);

            // Preparar lista inicial de reservas próximas
            doAnswer(invocation -> {
                Callback<List<Reserva>> callback = invocation.getArgument(0);
                callback.onSuccess(reservasProximas);
                return null;
            }).when(repository).getReservasUsuarioActual(any());

            // Clasificación para que se asignen las reservas próximas
            Map<String, List<Reserva>> clasificacion = new HashMap<>();
            clasificacion.put("proximas", reservasProximas);
            clasificacion.put("pasadas", new ArrayList<>());
            clasificacion.put("actual", new ArrayList<>());
            when(repository.clasificarReservas(any())).thenReturn(clasificacion);

            // Cargar las reservas iniciales
            when(repository.getCurrentUser()).thenReturn(mock(IFirebaseUser.class));
            viewModel.cargarReservasUsuario();

            // Simular la cancelación exitosa
            doAnswer(invocation -> {
                Callback<Void> callback = invocation.getArgument(1);
                callback.onSuccess(null);
                return null;
            }).when(repository).cancelarReserva(eq("1"), any());

            viewModel.cancelarReserva(reserva, 0, mockContext);

            // Verificaciones
            verify(isLoadingObserver, atLeastOnce()).onChanged(true);
            verify(isLoadingObserver, atLeastOnce()).onChanged(false);
            verify(operacionExitosaObserver).onChanged(true);
        }
    }

    @Test
    public void cancelarReserva_error() {
        // Crear reserva de prueba
        Reserva reserva = new Reserva();
        reserva.setId("1");
        reserva.setPlazaId(new Plaza(7L, "normal"));

        // Simular error del repositorio
        String errorMsg = "Error al cancelar";
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(1);
            callback.onError(errorMsg);
            return null;
        }).when(repository).cancelarReserva(anyString(), any());

        viewModel.cancelarReserva(reserva, 0, mockContext);

        // Verificaciones
        verify(isLoadingObserver).onChanged(true);
        verify(isLoadingObserver, atLeastOnce()).onChanged(false);
        verify(errorMessageObserver).onChanged("Error al cancelar la reserva: " + errorMsg);
    }

    @Test
    public void resetOperacionExitosa() {
        viewModel.resetOperacionExitosa();

        verify(operacionExitosaObserver).onChanged(null);
    }
}