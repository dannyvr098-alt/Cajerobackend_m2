package com.miplata.enums;

public enum TipoTransaccion {
    CONSIGNACION("Consignación"),
    RETIRO("Retiro"),
    TRANSFERENCIA_SALIDA("Transferencia Enviada"),
    TRANSFERENCIA_ENTRADA("Transferencia Recibida"),
    COMPRA_CREDITO("Compra con Tarjeta"),
    PAGO_CREDITO("Pago de Tarjeta");

    private final String descripcion;

    TipoTransaccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() { return descripcion; }
}
