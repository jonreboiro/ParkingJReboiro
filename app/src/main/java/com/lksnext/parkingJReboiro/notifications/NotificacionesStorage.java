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

    public static void guardarNotificacion(Context context, Notificacion notificacion) {
        List<Notificacion> lista = cargarNotificaciones(context);
        lista.add(0, notificacion); // Añadir al principio
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, new Gson().toJson(lista)).apply();
    }

    public static List<Notificacion> cargarNotificaciones(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Notificacion>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void guardarLista(Context context, List<Notificacion> lista) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, new Gson().toJson(lista)).apply();
    }
}