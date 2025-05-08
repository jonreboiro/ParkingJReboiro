package com.lksnext.parkingplantilla.domain;

public class Plaza {

    long id;
    String tipo;

    public Plaza() {

    }

    public Plaza(long id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
