package com.miplata.models;

import com.miplata.enums.EstadoCuenta;
import com.miplata.enums.TipoCuenta;
import com.miplata.enums.TipoTransaccion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public abstract class Cuenta {

    //  Atributos encapsulados
    private final String              id;
    private final String              numeroCuenta;
    private final TipoCuenta          tipo;
    private       EstadoCuenta        estado;
    private       double              saldo;
    private final String              propietarioId;
    private final List<Transaccion>   movimientos;   // List<T> requerido por el proyecto

    // Constructor protegido (solo las subclases)
    protected Cuenta(TipoCuenta tipo, String propietarioId, double saldoInicial) {
        if (tipo == null)
            throw new IllegalArgumentException("El tipo de cuenta no puede ser nulo.");
        if (propietarioId == null || propietarioId.isBlank())
            throw new IllegalArgumentException("El propietario es obligatorio.");
        if (saldoInicial < 0)
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");

        this.id            = generarId();
        this.numeroCuenta  = generarNumeroCuenta();
        this.tipo          = tipo;
        this.estado        = EstadoCuenta.ACTIVA;
        this.saldo         = saldoInicial;
        this.propietarioId = propietarioId;
        this.movimientos   = new ArrayList<>();
    }

    /** Cada subclase define cómo procesa un retiro (reglas propias). */
    public abstract double calcularRetiro(double monto) throws Exception;

    /** Cada subclase define si y cómo aplica intereses. */
    public abstract double aplicarInteres();

    /** Límite máximo que permite retirar la cuenta. */
    public abstract double getLimiteRetiro();

    // ════════════════════════════════════════════════════════════
    //  OPERACIONES COMUNES (heredadas por todas las subclases)
    // ════════════════════════════════════════════════════════════

    public final double consignar(double monto) throws Exception {
        validarActiva();
        if (monto <= 0)
            throw new Exception("El monto a consignar debe ser mayor a cero.");

        this.saldo += monto;
        registrarMovimiento(TipoTransaccion.CONSIGNACION, monto, "Consignación en " + tipo);
        return this.saldo;
    }

    public final double retirar(double monto) throws Exception {
        validarActiva();
        return calcularRetiro(monto);  // delegado al polimorfismo
    }

    public final void bloquear() { this.estado = EstadoCuenta.BLOQUEADA; }
    public final void activar()  { this.estado = EstadoCuenta.ACTIVA;    }
    public final void cerrar()   { this.estado = EstadoCuenta.CERRADA;   }

    //  MÉTODOS PROTEGIDOS (acceso desde subclases)

    protected void validarActiva() throws Exception {
        if (this.estado != EstadoCuenta.ACTIVA)
            throw new Exception("La cuenta " + numeroCuenta + " no está activa (" + estado + ").");
    }

    protected void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    protected Transaccion registrarMovimiento(TipoTransaccion tipo, double valor, String descripcion) {
        Transaccion txn = new Transaccion(tipo, valor, descripcion, this.saldo);
        movimientos.add(0, txn);  // más reciente al inicio
        return txn;
    }


    //  GETTERS PÚBLICOS


    public String            getId()            { return id; }
    public String            getNumeroCuenta()  { return numeroCuenta; }
    public TipoCuenta        getTipo()          { return tipo; }
    public EstadoCuenta      getEstado()        { return estado; }
    public double            getSaldo()         { return saldo; }
    public String            getPropietarioId() { return propietarioId; }

    /** Devuelve copia inmutable de la lista de movimientos. */
    public List<Transaccion> getMovimientos()   { return Collections.unmodifiableList(movimientos); }


    //  HELPERS PRIVADOS


    private static String generarId() {
        return "CTA-" + System.currentTimeMillis() + "-" +
               (char)('A' + new Random().nextInt(26));
    }

    private static String generarNumeroCuenta() {
        Random rnd = new Random();
        return String.format("%04d-%04d-%04d-%04d",
                rnd.nextInt(9000) + 1000,
                rnd.nextInt(9000) + 1000,
                rnd.nextInt(9000) + 1000,
                rnd.nextInt(9000) + 1000);
    }

    @Override
    public String toString() {
        return String.format("%s | N° %s | Saldo: $%,.2f | Estado: %s",
                tipo.getDescripcion(), numeroCuenta, saldo, estado);
    }
}
