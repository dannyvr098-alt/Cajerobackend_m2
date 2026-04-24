package com.miplata.models;

import com.miplata.enums.TipoTransaccion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Transaccion {

    private final String           id;
    private final LocalDateTime    fecha;
    private final TipoTransaccion  tipo;
    private final double           valor;
    private final String           descripcion;
    private final double           saldoResultante;

    public Transaccion(TipoTransaccion tipo, double valor, String descripcion, double saldoResultante) {
        if (tipo == null)
            throw new IllegalArgumentException("El tipo de transacción no puede ser nulo.");
        if (valor <= 0)
            throw new IllegalArgumentException("El valor de la transacción debe ser mayor a cero.");

        this.id              = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.fecha           = LocalDateTime.now();
        this.tipo            = tipo;
        this.valor           = valor;
        this.descripcion     = descripcion;
        this.saldoResultante = saldoResultante;
    }

    //  Getters
    public String          getId()              { return id; }
    public LocalDateTime   getFecha()           { return fecha; }
    public TipoTransaccion getTipo()            { return tipo; }
    public double          getValor()           { return valor; }
    public String          getDescripcion()     { return descripcion; }
    public double          getSaldoResultante() { return saldoResultante; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("[%s] %s | %s | $%,.2f | Saldo: $%,.2f | %s",
                id, fecha.format(fmt), tipo.getDescripcion(),
                valor, saldoResultante, descripcion);
    }
}
