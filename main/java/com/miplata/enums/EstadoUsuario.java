package com.miplata.enums;

public enum EstadoUsuario {
    ACTIVO("Activo"),
    BLOQUEADO("Bloqueado");

    private final String descripcion;

    EstadoUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() { return descripcion; }
}
