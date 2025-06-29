package com.lksnext.parkingJReboiro.notifications;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lksnext.parkingJReboiro.domain.Notificacion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificacionesStorage {
    private static final String PREFS_NAME = "notificaciones_real";
    private static final String KEY_LIST = "notificaciones_list";

    // Cambia el nombre de las preferencias y la clave usando el UID
    private static String getPrefsName(String userId) {
        return "notificaciones_real_" + userId;
    }

    public static void guardarNotificacion(Context context, Notificacion notificacion, String userId) {
        List<Notificacion> lista = cargarNotificaciones(context, userId);
        lista.add(0, notificacion);
        SharedPreferences prefs = context.getSharedPreferences(getPrefsName(userId), Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, new Gson().toJson(lista)).apply();
    }

    public static List<Notificacion> cargarNotificaciones(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefsName(userId), Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Notificacion>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void guardarLista(Context context, List<Notificacion> lista, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefsName(userId), Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, new Gson().toJson(lista)).apply();
    }
}