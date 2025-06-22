package com.lksnext.parkingJReboiro.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParkingMapView extends View {

    private static final String TAG = "ParkingMapView";
    private SVG svgParking;
    private Paint paint = new Paint();
    private Matrix matrix = new Matrix();
    private float scale = 1.0f;
    private float xTranslation = 0f;
    private float yTranslation = 0f;

    // Mapa de ID de plaza -> Rectángulo que la representa
    private HashMap<String, RectF> plazaBounds = new HashMap<>();

    // Estado de las plazas
    private Set<Long> plazasOcupadas;
    private long plazaSeleccionadaId = -1;

    // Colores de estado
    private final int COLOR_DISPONIBLE = Color.parseColor("#EBFFEE");
    private final int COLOR_OCUPADA = Color.parseColor("#FFCCCC");
    private static final int COLOR_SELECCIONADA = Color.parseColor("#4287f5");

    private Paint paintPatron;
    private static final float PATRON_ESPACIADO = 12f;

    // Listener para selecciones
    private OnPlazaSelectedListener listener;

    public interface OnPlazaSelectedListener {
        void onPlazaSelected(long plazaId, String tipo);
    }

    public ParkingMapView(Context context) {
        super(context);
        init(context);
    }

    public ParkingMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ParkingMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint.setAntiAlias(true);

        paintPatron = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPatron.setStrokeWidth(2f);
        paintPatron.setStyle(Paint.Style.STROKE);

        // Cargar el SVG
        try {
            InputStream is = context.getAssets().open("parking.svg");
            svgParking = SVG.getFromInputStream(is);
            is.close();

            // Analizar el SVG para encontrar todos los elementos de plaza
            analizarElementosPlazas();

            // Habilitar eventos de toque
            setClickable(true);

        } catch (IOException | SVGParseException e) {
            Log.e(TAG, "Error al cargar SVG", e);
        }
    }

    private void analizarElementosPlazas() {
        // Implementar análisis para extraer los IDs y bounds de las plazas
        // Buscar elementos con ID que comience con "plaza_"

        // Estos son ejemplos - en un caso real necesitarías recorrer todos los elementos del SVG
        // y extraer sus bounds o usar paths para generar bounds

        // Ejemplo de extracción de bounds para las plazas del SVG
        plazaBounds.put("plaza_1_minusvalido", new RectF(18, 24, 145, 96));
        plazaBounds.put("plaza_2_normal", new RectF(18, 134, 145, 206));
        plazaBounds.put("plaza_3_normal", new RectF(18, 233, 145, 305));
        plazaBounds.put("plaza_4_normal", new RectF(18, 332, 145, 404));
        plazaBounds.put("plaza_5_normal", new RectF(18, 431, 145, 503));
        plazaBounds.put("plaza_6_electrico", new RectF(18, 530, 145, 602));
        plazaBounds.put("plaza_7_minusvalido", new RectF(344, 24, 471, 96));
        plazaBounds.put("plaza_8_normal", new RectF(344, 134, 471, 206));
        plazaBounds.put("plaza_9_normal", new RectF(344, 233, 471, 305));
        plazaBounds.put("plaza_10_normal", new RectF(344, 332, 471, 404));
        plazaBounds.put("plaza_11_normal", new RectF(344, 431, 471, 503));
        plazaBounds.put("plaza_12_electrico", new RectF(344, 530, 471, 602));

        Log.d(TAG, "Plazas cargadas: " + plazaBounds.size());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (svgParking != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            float svgRatio = svgParking.getDocumentAspectRatio();
            int height = (int) (width / svgRatio);

            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (svgParking == null) return;

        // Escalar el SVG al tamaño de la vista
        float svgWidth = svgParking.getDocumentWidth();
        float svgHeight = svgParking.getDocumentHeight();
        scale = Math.min(getWidth() / svgWidth, getHeight() / svgHeight);

        matrix.reset();
        matrix.postScale(scale, scale);

        // Centrar SVG
        xTranslation = (getWidth() - (svgWidth * scale)) / 2;
        yTranslation = (getHeight() - (svgHeight * scale)) / 2;
        matrix.postTranslate(xTranslation, yTranslation);

        canvas.save();
        canvas.concat(matrix);

        // Primero dibujamos los rectángulos coloreados según el estado
        paint.setStyle(Paint.Style.FILL);

        // Dibujar plazas según su estado
        for (Map.Entry<String, RectF> entry : plazaBounds.entrySet()) {
            String plazaId = entry.getKey();
            RectF rect = entry.getValue();

            // Extraer el ID numérico de la plaza (por ejemplo, de "plaza_1_minusvalido" extraemos 1)
            Pattern pattern = Pattern.compile("plaza_(\\d+)_");
            Matcher matcher = pattern.matcher(plazaId);

            if (matcher.find()) {
                long idNumerico = Long.parseLong(matcher.group(1));

                // Determinar el color según el estado
                if (idNumerico == plazaSeleccionadaId) {
                    paint.setColor(COLOR_SELECCIONADA);
                    canvas.drawRect(rect, paint);
                    dibujarPatronPuntos(canvas, rect);
                } else if (plazasOcupadas != null && plazasOcupadas.contains(idNumerico)) {
                    paint.setColor(COLOR_OCUPADA);
                    canvas.drawRect(rect, paint);
                    dibujarPatronDiagonales(canvas, rect);
                } else {
                    paint.setColor(COLOR_DISPONIBLE);
                    canvas.drawRect(rect, paint);
                }

            }
        }

        // Luego dibujamos el SVG encima
        svgParking.renderToCanvas(canvas);

        canvas.restore();
    }

    private void dibujarPatronPuntos(Canvas canvas, RectF rect) {
        paintPatron.setColor(Color.WHITE);
        paintPatron.setStyle(Paint.Style.FILL);

        float startX = rect.left + PATRON_ESPACIADO/2;
        float startY = rect.top + PATRON_ESPACIADO/2;

        for (float x = startX; x <= rect.right; x += PATRON_ESPACIADO) {
            for (float y = startY; y <= rect.bottom; y += PATRON_ESPACIADO) {
                canvas.drawCircle(x, y, 2f, paintPatron);
            }
        }
    }

    private void dibujarPatronDiagonales(Canvas canvas, RectF rect) {
        paintPatron.setColor(Color.WHITE);
        paintPatron.setStyle(Paint.Style.STROKE);
        paintPatron.setStrokeWidth(2f);

        // Dibujar líneas diagonales de esquina a esquina
        for (float offset = 0; offset < rect.width() + rect.height(); offset += PATRON_ESPACIADO) {
            // Líneas descendentes (esquina superior izquierda a inferior derecha)
            float startX = Math.max(rect.left, rect.left + offset - rect.height());
            float startY = Math.min(rect.top + offset, rect.bottom);
            float stopX = Math.min(rect.left + offset, rect.right);
            float stopY = Math.max(rect.top, rect.top + offset - rect.width());

            canvas.drawLine(startX, startY, stopX, stopY, paintPatron);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Convertir las coordenadas del toque al espacio del SVG
            float touchX = (event.getX() - xTranslation) / scale;
            float touchY = (event.getY() - yTranslation) / scale;

            // Buscar si se tocó alguna plaza
            for (Map.Entry<String, RectF> entry : plazaBounds.entrySet()) {
                if (entry.getValue().contains(touchX, touchY)) {
                    String plazaId = entry.getKey();

                    // Extraer ID numérico y tipo
                    Pattern pattern = Pattern.compile("plaza_(\\d+)_(.+)");
                    Matcher matcher = pattern.matcher(plazaId);

                    if (matcher.find()) {
                        long idNumerico = Long.parseLong(matcher.group(1));
                        String tipo = matcher.group(2);

                        // Verificar si la plaza está ocupada
                        if (plazasOcupadas != null && plazasOcupadas.contains(idNumerico)) {
                            // No permitir selección de plazas ocupadas
                            return true;
                        }

                        // Actualizar selección
                        plazaSeleccionadaId = idNumerico;

                        // Notificar al listener
                        if (listener != null) {
                            listener.onPlazaSelected(idNumerico, tipo);
                        }

                        // Redibujar la vista
                        invalidate();

                        Log.d(TAG, "Plaza seleccionada: " + idNumerico + " tipo: " + tipo);
                        return true;
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public void setOnPlazaSelectedListener(OnPlazaSelectedListener listener) {
        this.listener = listener;
    }

    public void setPlazasOcupadas(Set<Long> plazasOcupadas) {
        this.plazasOcupadas = plazasOcupadas;

        // Si la plaza seleccionada ahora está ocupada, desseleccionarla
        if (plazasOcupadas != null && plazaSeleccionadaId != -1 &&
                plazasOcupadas.contains(plazaSeleccionadaId)) {
            plazaSeleccionadaId = -1;
        }

        invalidate();
    }
}