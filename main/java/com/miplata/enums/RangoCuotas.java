package com.miplata.enums;

public enum RangoCuotas {
    SIN_INTERES(1, 2, 0.00, "≤ 2 cuotas — Sin interés (0%)"),
    INTERES_MODERADO(3, 6, 0.019, "3–6 cuotas — Interés moderado (1.9%)"),
    INTERES_ALTO(7, Integer.MAX_VALUE, 0.023, "≥ 7 cuotas — Interés alto (2.3%)");

    private final int minCuotas;
    private final int maxCuotas;
    private final double tasaMensual;
    private final String descripcion;

    RangoCuotas(int minCuotas, int maxCuotas, double tasaMensual, String descripcion) {
        this.minCuotas   = minCuotas;
        this.maxCuotas   = maxCuotas;
        this.tasaMensual = tasaMensual;
        this.descripcion = descripcion;
    }

    public double getTasaMensual() { return tasaMensual; }
    public String getDescripcion() { return descripcion; }

    /** Aquí se devuelve el rango correspondiente al número de cuotas dado. */
    public static RangoCuotas obtenerPorCuotas(int cuotas) {
        for (RangoCuotas r : values()) {
            if (cuotas >= r.minCuotas && cuotas <= r.maxCuotas) return r;
        }
        throw new IllegalArgumentException("Número de cuotas inválido: " + cuotas);
    }
}
