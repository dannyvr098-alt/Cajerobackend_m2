package com.miplata.enums;

public enum TipoCuenta {
    AHORROS("Cuenta de Ahorros"),
    CORRIENTE("Cuenta Corriente"),
    CREDITO("Tarjeta de Crédito");

    private final String descripcion;

    TipoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() { return descripcion; }
}
