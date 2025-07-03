package com.lksnext.parkingJReboiro.domain;

public class ReservaConTiempo {
    private Reserva reserva;
    private String tiempoRestante;

    public ReservaConTiempo(Reserva reserva, String tiempoRestante) {
        this.reserva = reserva;
        this.tiempoRestante = tiempoRestante;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public String getTiempoRestante() {
        return tiempoRestante;
    }

    public void setTiempoRestante(String tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
    }
}