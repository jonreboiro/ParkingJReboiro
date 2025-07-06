package com.lksnext.parkingJReboiro.viewmodel;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.notifications.NotificationScheduler;
import com.lksnext.parkingJReboiro.util.ITimer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NuevaReservaViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IDataRepository mockRepository;

    @Mock
    private Observer<Boolean> cargandoObserver;

    @Mock
    private Observer<String> mensajeErrorObserver;

    private Context testContext;

    @Mock
    private Observer<Boolean> reservaExitosaObserver;

    @Mock
    private Observer<Plaza> plazaDisponibleObserver;

    @Mock
    private Observer<Boolean> busquedaIntentadaObserver;

    @Mock
    private Observer<Set<Long>> plazasOcupadasObserver;

    @Captor
    private ArgumentCaptor<Callback<String>> reservaCallbackCaptor;

    @Captor
    private ArgumentCaptor<Callback<Plaza>> plazaCallbackCaptor;

    @Captor
    private ArgumentCaptor<Callback<Set<Long>>> plazasOcupadasCallbackCaptor;

    private NuevaReservaViewModel viewModel;

    private final String FECHA = "07/07/2025";
    private final Long PLAZA_ID = 5L;
    private final String MATRICULA = "1234BCD";
    private final String TIPO_PLAZA = "normal";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        viewModel = new NuevaReservaViewModel(mockRepository);

        viewModel.getCargando().observeForever(cargandoObserver);
        viewModel.getMensajeError().observeForever(mensajeErrorObserver);
        viewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        viewModel.getPlazaDisponible().observeForever(plazaDisponibleObserver);
        viewModel.getBusquedaIntentada().observeForever(busquedaIntentadaObserver);
        viewModel.getPlazasOcupadas().observeForever(plazasOcupadasObserver);

        viewModel.setTimerFactory((millis, interval, onTick, onFinish) -> new ITimer() {
            @Override
            public CountDownTimer start() {
                return new CountDownTimer(1, 1) {
                    @Override public void onTick(long millisUntilFinished) {}
                    @Override public void onFinish() {}
                };
            }
            @Override
            public void cancel() {}
        });

        testContext = new TestMockContext();
    }

    @Test
    public void verificarYGuardarReserva_success() {
        try (MockedStatic<NotificationScheduler> mocked = mockStatic(NotificationScheduler.class)) {
            configurarDatosReservaValida();

            doAnswer(invocation -> {
                Callback<String> callback = invocation.getArgument(2);
                callback.onSuccess("reserva123");
                return null;
            }).when(mockRepository).verificarYGuardarReserva(any(Reserva.class), anyBoolean(), any(Callback.class));

            viewModel.verificarYGuardarReserva(testContext);

            verify(cargandoObserver).onChanged(true);
            verify(cargandoObserver, atLeastOnce()).onChanged(false);
            verify(reservaExitosaObserver).onChanged(true);

            assertThat(viewModel.getReservaExitosa().getValue(), is(true));
        }
    }

    @Test
    public void verificarYGuardarReserva_error() {
        // Configurar datos iniciales
        configurarDatosReservaValida();

        String errorMsg = "Plaza ya reservada";

        // Simular error en el repositorio
        doAnswer(invocation -> {
            Callback<String> callback = invocation.getArgument(2);
            callback.onError(errorMsg);
            return null;
        }).when(mockRepository).verificarYGuardarReserva(any(Reserva.class), anyBoolean(), any(Callback.class));

        viewModel.verificarYGuardarReserva(testContext);

        // Verificaciones
        verify(cargandoObserver).onChanged(true);
        verify(cargandoObserver, atLeastOnce()).onChanged(false);
        verify(mensajeErrorObserver).onChanged(errorMsg);

        assertThat(viewModel.getMensajeError().getValue(), equalTo(errorMsg));
    }

    @Test
    public void verificarYGuardarReserva_matriculaInvalida() {
        // Configurar datos válidos pero matrícula incorrecta
        configurarDatosReservaValida();
        viewModel.setMatricula("ABCD123"); // Formato incorrecto

        viewModel.verificarYGuardarReserva(testContext);

        // Verificar que se detectó el error de formato
        verify(mensajeErrorObserver).onChanged(any(String.class));
        verify(mockRepository, never()).verificarYGuardarReserva(any(), anyBoolean(), any());

        assertThat(viewModel.getMensajeError().getValue(), containsString("formato de la matrícula"));
    }

    @Test
    public void buscarPlazaDisponible_success() {
        // Configurar datos para búsqueda
        viewModel.setFecha(FECHA);
        viewModel.setHoraInicio(10);
        viewModel.setMinutosInicio(0);
        viewModel.setDuracion(2);

        // Crear plaza resultado
        Plaza plaza = new Plaza(PLAZA_ID, TIPO_PLAZA);

        // Simular repositorio
        doAnswer(invocation -> {
            Callback<Plaza> callback = invocation.getArgument(4);
            callback.onSuccess(plaza);
            return null;
        }).when(mockRepository).buscarPlazaDisponible(anyString(), anyLong(), anyLong(), anyString(), any(Callback.class));

        viewModel.buscarPlazaDisponible(TIPO_PLAZA);

        // Verificaciones
        verify(cargandoObserver).onChanged(true);
        verify(cargandoObserver, atLeastOnce()).onChanged(false);
        verify(plazaDisponibleObserver).onChanged(plaza);
        verify(busquedaIntentadaObserver).onChanged(true);

        assertThat(viewModel.getPlazaDisponible().getValue(), is(plaza));
        assertThat(viewModel.getBusquedaIntentada().getValue(), is(true));
        assertThat(viewModel.getPlazaId().getValue(), equalTo(PLAZA_ID));
    }

    @Test
    public void buscarPlazaDisponible_notFound() {
        // Configurar datos
        viewModel.setFecha(FECHA);
        viewModel.setHoraInicio(10);
        viewModel.setMinutosInicio(0);
        viewModel.setDuracion(2);

        // Simular que no se encuentra plaza
        doAnswer(invocation -> {
            Callback<Plaza> callback = invocation.getArgument(4);
            callback.onSuccess(null);
            return null;
        }).when(mockRepository).buscarPlazaDisponible(anyString(), anyLong(), anyLong(), anyString(), any(Callback.class));

        viewModel.buscarPlazaDisponible(TIPO_PLAZA);

        // Verificaciones
        verify(cargandoObserver).onChanged(true);
        verify(cargandoObserver, atLeastOnce()).onChanged(false);
        verify(plazaDisponibleObserver).onChanged(null);
        verify(busquedaIntentadaObserver).onChanged(true);

        assertThat(viewModel.getPlazaDisponible().getValue(), is(nullValue()));
        assertThat(viewModel.getBusquedaIntentada().getValue(), is(true));
    }

    @Test
    public void cargarPlazasOcupadas_success() {
        // Configurar datos
        viewModel.setFecha(FECHA);
        viewModel.setHoraInicio(10);
        viewModel.setMinutosInicio(0);
        viewModel.setDuracion(2);

        // Crear set de plazas ocupadas
        Set<Long> plazasOcupadas = new HashSet<>();
        plazasOcupadas.add(1L);
        plazasOcupadas.add(3L);

        // Simular repositorio
        doAnswer(invocation -> {
            Callback<Set<Long>> callback = invocation.getArgument(4);
            callback.onSuccess(plazasOcupadas);
            return null;
        }).when(mockRepository).getPlazasOcupadasPorPlanta(anyString(), anyLong(), anyLong(), anyInt(), any(Callback.class));

        viewModel.cargarPlazasOcupadas(1);

        // Verificaciones
        verify(cargandoObserver).onChanged(true);
        verify(cargandoObserver, atLeastOnce()).onChanged(false);
        verify(plazasOcupadasObserver).onChanged(plazasOcupadas);

        assertThat(viewModel.getPlazasOcupadas().getValue(), is(plazasOcupadas));
    }

    @Test
    public void calcularHorario_test() {
        // Configurar datos
        viewModel.setHoraInicio(10);
        viewModel.setMinutosInicio(30);
        viewModel.setDuracion(2);

        // Calcular horario
        NuevaReservaViewModel.HorarioCalculado horario = viewModel.calcularHorario();

        // Verificaciones
        assertThat(horario, notNullValue());
        assertThat(horario.getHoraInicio(), is(10));
        assertThat(horario.getMinInicio(), is(30));
        assertThat(horario.getHoraFin(), is(12));
        assertThat(horario.getMinFin(), is(30));
        assertThat(horario.getHoraInicioFormateada(), equalTo("10:30"));
        assertThat(horario.getHoraFinFormateada(), equalTo("12:30"));
    }

    @Test
    public void validarSeleccion_datosIncompletos() {
        // Sin configurar datos
        boolean resultado = viewModel.validarSeleccion();

        // Verificación
        assertThat(resultado, is(false));
        assertThat(viewModel.getMensajeError().getValue(), containsString("Selecciona fecha y hora"));
    }

    @Test
    public void validarSeleccion_fechaPasada() {
        // Configurar fecha pasada
        Calendar pasado = Calendar.getInstance();
        pasado.add(Calendar.DAY_OF_MONTH, -1);

        viewModel.setSelectedDate(
                pasado.get(Calendar.YEAR),
                pasado.get(Calendar.MONTH),
                pasado.get(Calendar.DAY_OF_MONTH)
        );
        viewModel.setHoraInicio(10);

        // Validar
        boolean resultado = viewModel.validarSeleccion();

        // Verificación
        assertThat(resultado, is(false));
        assertThat(viewModel.getMensajeError().getValue(), containsString("No puedes seleccionar una fecha/hora pasada"));
    }

    @Test
    public void esMatriculaEspanolaValida_test() {
        assertThat(viewModel.esMatriculaEspanolaValida("1234BCD"), is(true));
        assertThat(viewModel.esMatriculaEspanolaValida("1234BDF"), is(true));
        assertThat(viewModel.esMatriculaEspanolaValida("1234ABC"), is(false)); // A no permitida
        assertThat(viewModel.esMatriculaEspanolaValida("0000AAA"), is(false)); // A no permitida
        assertThat(viewModel.esMatriculaEspanolaValida("123ABCD"), is(false)); // Formato incorrecto
        assertThat(viewModel.esMatriculaEspanolaValida("ABCD123"), is(false)); // Formato incorrecto
        assertThat(viewModel.esMatriculaEspanolaValida(null), is(false));
        assertThat(viewModel.esMatriculaEspanolaValida(""), is(false));
    }

    @Test
    public void getTipoPlazaTexto_test() {
        assertThat(viewModel.getTipoPlazaTexto("normal"), equalTo("Estándar"));
        assertThat(viewModel.getTipoPlazaTexto("minusvalido"), equalTo("Minusválidos"));
        assertThat(viewModel.getTipoPlazaTexto("electrico"), equalTo("Vehículo eléctrico"));
        assertThat(viewModel.getTipoPlazaTexto("moto"), equalTo("Moto"));
        assertThat(viewModel.getTipoPlazaTexto(null), equalTo("Estándar"));
    }

    @Test
    public void reiniciar_test() {
        // Configurar datos
        configurarDatosReservaValida();

        // Reiniciar
        viewModel.reiniciar();

        // Verificaciones
        assertThat(viewModel.getFecha().getValue(), nullValue());
        assertThat(viewModel.getHoraInicio().getValue(), nullValue());
        assertThat(viewModel.getMinutosInicio().getValue(), equalTo(0));
        assertThat(viewModel.getDuracion().getValue(), equalTo(1));
        assertThat(viewModel.getPlazaId().getValue(), nullValue());
        assertThat(viewModel.getTipoPlaza().getValue(), nullValue());
        assertThat(viewModel.getPlazasOcupadas().getValue().isEmpty(), is(true));
        assertThat(viewModel.getMensajeError().getValue(), nullValue());
        assertThat(viewModel.getCargando().getValue(), is(false));
        assertThat(viewModel.getReservaExitosa().getValue(), is(false));
        assertThat(viewModel.getPlazaDisponible().getValue(), nullValue());
        assertThat(viewModel.getBusquedaIntentada().getValue(), is(false));
    }

    @Test
    public void cargarPlazasOcupadas_error() {
        // Configurar datos
        viewModel.setFecha(FECHA);
        viewModel.setHoraInicio(10);
        viewModel.setMinutosInicio(0);
        viewModel.setDuracion(2);

        String errorMsg = "Error de conexión";

        // Simular error en el repositorio
        doAnswer(invocation -> {
            Callback<Set<Long>> callback = invocation.getArgument(4);
            callback.onError(errorMsg);
            return null;
        }).when(mockRepository).getPlazasOcupadasPorPlanta(anyString(), anyLong(), anyLong(), anyInt(), any(Callback.class));

        viewModel.cargarPlazasOcupadas(1);

        // Verificaciones
        verify(cargandoObserver).onChanged(true);
        verify(cargandoObserver, atLeastOnce()).onChanged(false);
        verify(mensajeErrorObserver).onChanged(any(String.class));

        assertThat(viewModel.getMensajeError().getValue(), containsString(errorMsg));
    }

    @Test
    public void validarSeleccion_duracionInvalida() {
        // Configurar fecha válida pero duración inválida
        Calendar futuro = Calendar.getInstance();
        futuro.add(Calendar.DAY_OF_MONTH, 1);

        viewModel.setSelectedDate(
                futuro.get(Calendar.YEAR),
                futuro.get(Calendar.MONTH),
                futuro.get(Calendar.DAY_OF_MONTH)
        );
        viewModel.setHoraInicio(10);
        viewModel.setDuracion(9); // Más de 8 horas (inválido)

        // Validar
        boolean resultado = viewModel.validarSeleccion();

        // Verificación
        assertThat(resultado, is(false));
        assertThat(viewModel.getMensajeError().getValue(), containsString("Duración debe ser entre 1 y 8 horas"));
    }

    @Test
    public void buscarPlazaDisponible_error() {
        // Configurar datos
        viewModel.setFecha(FECHA);
        viewModel.setHoraInicio(10);
        viewModel.setMinutosInicio(0);
        viewModel.setDuracion(2);

        String errorMsg = "Error de conexión";

        // Simular error en el repositorio
        doAnswer(invocation -> {
            Callback<Plaza> callback = invocation.getArgument(4);
            callback.onError(errorMsg);
            return null;
        }).when(mockRepository).buscarPlazaDisponible(anyString(), anyLong(), anyLong(), anyString(), any(Callback.class));

        viewModel.buscarPlazaDisponible(TIPO_PLAZA);

        // Verificaciones
        verify(cargandoObserver).onChanged(true);
        verify(cargandoObserver, atLeastOnce()).onChanged(false);
        verify(mensajeErrorObserver).onChanged(any(String.class));
        verify(busquedaIntentadaObserver).onChanged(true);

        assertThat(viewModel.getMensajeError().getValue(), containsString(errorMsg));
        assertThat(viewModel.getBusquedaIntentada().getValue(), is(true));
    }

    @Test
    public void limpiarMensajeError_test() {
        // Configurar un mensaje de error
        viewModel.setMatricula("ABCD123");
        viewModel.verificarYGuardarReserva(testContext);

        assertThat(viewModel.getMensajeError().getValue(), notNullValue());

        // Limpiar mensaje
        viewModel.limpiarMensajeError();

        // Verificar que se limpió
        assertThat(viewModel.getMensajeError().getValue(), nullValue());
    }

    private void configurarDatosReservaValida() {
        // Configuramos fecha futura
        Calendar futuro = Calendar.getInstance();
        futuro.add(Calendar.MONTH, 1);
        viewModel.setSelectedDate(
                futuro.get(Calendar.YEAR),
                futuro.get(Calendar.MONTH),
                futuro.get(Calendar.DAY_OF_MONTH)
        );

        viewModel.setFecha(FECHA);
        viewModel.setHoraInicio(10);
        viewModel.setMinutosInicio(0);
        viewModel.setDuracion(2);
        viewModel.seleccionarPlaza(PLAZA_ID, TIPO_PLAZA);
        viewModel.setMatricula(MATRICULA);
    }

}