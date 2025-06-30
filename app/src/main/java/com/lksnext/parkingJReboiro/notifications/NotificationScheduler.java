package com.lksnext.parkingJReboiro.notifications;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.lksnext.parkingJReboiro.domain.Notificacion;
import com.google.firebase.auth.FirebaseAuth;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.lksnext.parkingJReboiro.R;

public class NotificationScheduler {
    private static final String CHANNEL_ID = "reservas_channel";

    public static void scheduleNotification(Context context, long triggerAtMillis, int notificationId, String titulo, String mensaje) {
        Intent intent = new Intent(context, ReservaNotificationReceiver.class);
        intent.putExtra("titulo", titulo);
        intent.putExtra("mensaje", mensaje);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                Log.e("NotificationScheduler", "No se pueden programar alarmas exactas: permiso denegado");
            }
        } catch (SecurityException e) {
            Log.e("NotificationScheduler", "No se pudo programar la alarma exacta: permiso denegado", e);
        }
    }

    public static void showInstantNotification(Context context, int notificationId, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.lks_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, builder.build());
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                NotificacionesStorage.guardarNotificacion(context, new Notificacion(
                        notificationId, title, message, new java.util.Date(), false
                ), userId);
            }
        } catch (SecurityException e) {
            Log.e("NotificationScheduler", "No se pudo mostrar la notificación: permiso denegado", e);
        }
    }
}