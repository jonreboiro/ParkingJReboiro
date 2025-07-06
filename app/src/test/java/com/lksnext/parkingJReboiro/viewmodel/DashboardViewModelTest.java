package com.lksnext.parkingJReboiro.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.Hora;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.domain.ReservaConTiempo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DashboardViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IDataRepository mockDataRepository;

    @Mock
    private Observer<List<ReservaConTiempo>> reservasObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<String> errorObserver;

    @Captor
    private ArgumentCaptor<Callback<List<Reserva>>> callbackCaptor;

    private DashboardViewModel viewModel;

    private final String FECHA = "06/07/2024";
    private final Long PLAZA_ID = 12L;
    private final String USUARIO_ID = "user123";
    private final String RESERVA_ID = "r1";
    private final String MATRICULA = "1234BCD";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        viewModel = new DashboardViewModel(mockDataRepository);

        viewModel.getReservasActivas().observeForever(reservasObserver);
        viewModel.isLoading().observeForever(loadingObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);

        Mockito.clearInvocations(loadingObserver);
        Mockito.clearInvocations(mockDataRepository);
    }

    @Test
    public void getActiveReservations_success() {
        // Preparar datos de prueba
        List<Reserva> reservas = crearListaDeReservas();

        // Configurar comportamiento del mock
        Map<String, List<Reserva>> clasificadas = new HashMap<>();
        clasificadas.put("actual", reservas);
        when(mockDataRepository.clasificarReservas(reservas)).thenReturn(clasificadas);

        doAnswer(invocation -> {
            Callback<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(reservas);
            return null;
        }).when(mockDataRepository).getReservasUsuarioActual(any(Callback.class));

        viewModel.cargarReservasActivas();

        // Verificaciones
        verify(mockDataRepository).getReservasUsuarioActual(callbackCaptor.capture());
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);

        // Verificar resultado
        List<ReservaConTiempo> resultado = viewModel.getReservasActivas().getValue();
        assertThat(resultado, notNullValue());
        assertThat(resultado, hasSize(1));
        assertThat(resultado.get(0).getReserva().getPlazaId().getId(), equalTo(PLAZA_ID));
        assertThat(resultado.get(0).getReserva().getMatricula(), equalTo(MATRICULA));
    }

    @Test
    public void getActiveReservations_error() {
        // Configurar error
        String errorMsg = "DB error";
        doAnswer(invocation -> {
            Callback<List<Reserva>> callback = invocation.getArgument(0);
            callback.onError(errorMsg);
            return null;
        }).when(mockDataRepository).getReservasUsuarioActual(any(Callback.class));

        viewModel.cargarReservasActivas();

        // Verificaciones
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(errorObserver).onChanged(any(String.class));

        // Verificar mensaje de error
        String actualErrorMsg = viewModel.getErrorMessage().getValue();
        assertThat(actualErrorMsg, containsString(errorMsg));
    }

    @Test
    public void getActiveReservations_emptyList() {
        // Lista vacía de reservas
        List<Reserva> reservasVacias = new ArrayList<>();

        Map<String, List<Reserva>> clasificadas = new HashMap<>();
        clasificadas.put("actual", new ArrayList<>());
        when(mockDataRepository.clasificarReservas(reservasVacias)).thenReturn(clasificadas);

        doAnswer(invocation -> {
            Callback<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(reservasVacias);
            return null;
        }).when(mockDataRepository).getReservasUsuarioActual(any(Callback.class));

        viewModel.cargarReservasActivas();

        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);

        List<ReservaConTiempo> resultado = viewModel.getReservasActivas().getValue();
        assertThat(resultado, notNullValue());
        assertThat(resultado, hasSize(0));
    }

    @Test
    public void refreshActiveReservations_callsLoadActiveReservations() {
        doAnswer(invocation -> {
            Callback<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(new ArrayList<>());
            return null;
        }).when(mockDataRepository).getReservasUsuarioActual(any(Callback.class));

        viewModel.refrescarReservasActivas();

        verify(mockDataRepository).getReservasUsuarioActual(any(Callback.class));
    }

    private List<Reserva> crearListaDeReservas() {
        List<Reserva> reservas = new ArrayList<>();

        Plaza plaza = new Plaza();
        plaza.setId(PLAZA_ID);

        // 9:00 - 11:00 (en milisegundos)
        Hora hora = new Hora(9 * 3600 * 1000, 11 * 3600 * 1000);

        Reserva reserva = new Reserva(FECHA, USUARIO_ID, RESERVA_ID, plaza, hora, MATRICULA);
        reservas.add(reserva);

        return reservas;
    }
}