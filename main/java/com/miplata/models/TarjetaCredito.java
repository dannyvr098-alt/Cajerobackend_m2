package com.miplata.models;

import com.miplata.enums.RangoCuotas;
import com.miplata.enums.TipoCuenta;
import com.miplata.enums.TipoTransaccion;


public class TarjetaCredito extends Cuenta {

    private final double cupo;
    private       double deuda;

    public TarjetaCredito(String propietarioId, double cupo) {
        super(TipoCuenta.CREDITO, propietarioId, 0);
        if (cupo <= 0)
            throw new IllegalArgumentException("El cupo de crédito debe ser mayor a cero.");
        this.cupo  = cupo;
        this.deuda = 0;
    }

    // Getters específicos
    public double getCupo()           { return cupo; }
    public double getDeuda()          { return deuda; }
    public double getCupoDisponible() { return cupo - deuda; }

    // Polimorfismo para: calcularRetiro — no aplica
    @Override
    public double calcularRetiro(double monto) throws Exception {
        throw new Exception("Las tarjetas de crédito no permiten retiros en efectivo. Use realizarCompra().");
    }

    // Polimorfismo para: aplicarInteres — no aplica directamente
    @Override
    public double aplicarInteres() {
        return 0;  // El interés se calcula por compra/cuotas
    }

    @Override
    public double getLimiteRetiro() { return 0; }

    //  Operación principal: realizar compra
    public ResultadoCompra realizarCompra(double monto, int cuotas) throws Exception {
        validarActiva();
        if (monto <= 0)
            throw new Exception("El monto de compra debe ser mayor a cero.");
        if (cuotas < 1)
            throw new Exception("El número de cuotas debe ser al menos 1.");
        if (monto > getCupoDisponible())
            throw new Exception(String.format(
                "Cupo insuficiente. Cupo disponible: $%,.2f | Monto solicitado: $%,.2f",
                getCupoDisponible(), monto));

        RangoCuotas rango      = RangoCuotas.obtenerPorCuotas(cuotas);
        double      tasa       = rango.getTasaMensual();
        double      cuotaMens  = calcularCuotaMensual(monto, tasa, cuotas);

        this.deuda += monto;

        registrarMovimiento(TipoTransaccion.COMPRA_CREDITO, monto,
                String.format("Compra a %d cuota(s) | Tasa: %.1f%% | Cuota mensual: $%,.2f | %s",
                        cuotas, tasa * 100, cuotaMens, rango.getDescripcion()));

        return new ResultadoCompra(monto, cuotas, tasa, cuotaMens, this.deuda);
    }

    public double pagarDeuda(double monto) throws Exception {
        validarActiva();
        if (monto <= 0)
            throw new Exception("El pago debe ser mayor a cero.");
        if (monto > this.deuda)
            throw new Exception(String.format(
                "El pago ($%,.2f) supera la deuda actual ($%,.2f).", monto, this.deuda));

        this.deuda -= monto;
        registrarMovimiento(TipoTransaccion.PAGO_CREDITO, monto,
                String.format("Pago a tarjeta. Deuda restante: $%,.2f", this.deuda));
        return this.deuda;
    }

    // Fórmula de cuota mensual
    public static double calcularCuotaMensual(double capital, double tasa, int n) {
        if (tasa == 0) return capital / n;
        return (capital * tasa) / (1 - Math.pow(1 + tasa, -n));
    }

    // Inner class para resultado de compra
    public static class ResultadoCompra {
        public final double monto;
        public final int    cuotas;
        public final double tasa;
        public final double cuotaMensual;
        public final double deudaTotal;

        public ResultadoCompra(double monto, int cuotas, double tasa, double cuotaMensual, double deudaTotal) {
            this.monto        = monto;
            this.cuotas       = cuotas;
            this.tasa         = tasa;
            this.cuotaMensual = cuotaMensual;
            this.deudaTotal   = deudaTotal;
        }

        @Override
        public String toString() {
            return String.format(
                "Compra: $%,.2f | %d cuota(s) | Tasa: %.1f%% mensual | Cuota mensual: $%,.2f | Deuda total: $%,.2f",
                monto, cuotas, tasa * 100, cuotaMensual, deudaTotal);
        }
    }
}
