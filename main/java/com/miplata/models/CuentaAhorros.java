package com.miplata.models;

import com.miplata.enums.TipoCuenta;
import com.miplata.enums.TipoTransaccion;



public class CuentaAhorros extends Cuenta {

    public static final double TASA_MENSUAL = 0.015;  // 1.5%

    public CuentaAhorros(String propietarioId, double saldoInicial) {
        super(TipoCuenta.AHORROS, propietarioId, saldoInicial);
    }

    // Polimorfismo: calcularRetiro
    @Override
    public double calcularRetiro(double monto) throws Exception {
        if (monto <= 0)
            throw new Exception("El monto a retirar debe ser mayor a cero.");

        double saldoConInteres = getSaldo() * (1 + TASA_MENSUAL);

        if (monto > saldoConInteres)
            throw new Exception(String.format(
                "Saldo insuficiente. Saldo actual: $%,.2f | Límite con interés (1.5%%): $%,.2f",
                getSaldo(), saldoConInteres));

        setSaldo(getSaldo() - monto);
        registrarMovimiento(TipoTransaccion.RETIRO, monto,
                String.format("Retiro Ahorros — Interés 1.5%% aplicado. Límite era: $%,.2f", saldoConInteres));
        return getSaldo();
    }

    // Polimorfismo para: aplicarInteres
    @Override
    public double aplicarInteres() {
        double interes = getSaldo() * TASA_MENSUAL;
        setSaldo(getSaldo() + interes);
        registrarMovimiento(TipoTransaccion.CONSIGNACION, interes,
                String.format("Rendimiento mensual 1.5%% — Interés: $%,.2f", interes));
        return interes;
    }

    @Override
    public double getLimiteRetiro() {
        return getSaldo() * (1 + TASA_MENSUAL);
    }
}
