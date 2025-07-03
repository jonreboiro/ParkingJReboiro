package com.lksnext.parkingJReboiro.util;

public class PlazaUtils {
    public static String getTipoPlazaFormal(String tipo) {
        if (tipo == null) return "Estándar";
        switch (tipo) {
            case "minusvalido": return "Minusválido";
            case "electrico": return "Carga eléctrica";
            case "moto": return "Motocicleta";
            case "normal":
            default: return "Estándar";
        }
    }
}