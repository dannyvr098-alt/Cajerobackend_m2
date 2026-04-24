package com.miplata.enums;

public enum EstadoCuenta {
    ACTIVA("Activa"),
    BLOQUEADA("Bloqueada"),
    CERRADA("Cerrada");

    private final String descripcion;

    EstadoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() { return descripcion; }
}
