package com.miplata.models;

import com.miplata.enums.TipoCuenta;
import com.miplata.enums.TipoTransaccion;


public class CuentaCorriente extends Cuenta {

    public static final double PORCENTAJE_SOBREGIRO = 0.20;  // 20%

    public CuentaCorriente(String propietarioId, double saldoInicial) {
        super(TipoCuenta.CORRIENTE, propietarioId, saldoInicial);
    }

    // Polimorfismo para: calcularRetiro
    @Override
    public double calcularRetiro(double monto) throws Exception {
        if (monto <= 0)
            throw new Exception("El monto a retirar debe ser mayor a cero.");

        double limite = getLimiteRetiro();
        if (monto > limite)
            throw new Exception(String.format(
                "Supera el límite de retiro. Saldo: $%,.2f | Límite con sobregiro (20%%): $%,.2f",
                getSaldo(), limite));

        setSaldo(getSaldo() - monto);
        registrarMovimiento(TipoTransaccion.RETIRO, monto,
                String.format("Retiro Corriente — Sobregiro 20%% permitido. Límite era: $%,.2f", limite));
        return getSaldo();
    }

    // Polimorfismo para: aplicarInteres — no genera
    @Override
    public double aplicarInteres() {
        return 0;  // La cuenta corriente no genera intereses
    }

    @Override
    public double getLimiteRetiro() {
        return getSaldo() * (1 + PORCENTAJE_SOBREGIRO);
    }
}
