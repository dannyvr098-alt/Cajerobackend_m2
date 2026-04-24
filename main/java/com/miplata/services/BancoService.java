package com.miplata.services;

import com.miplata.enums.TipoCuenta;
import com.miplata.models.*;
import com.miplata.repositories.ClienteRepository;
import com.miplata.repositories.CuentaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio principal del banco.
 * Orquesta las operaciones usando los repositorios.
 * Gestiona la sesión activa del usuario.
 */
public class BancoService {

    private final ClienteRepository clienteRepo;
    private final CuentaRepository  cuentaRepo;

    // Sesión activa
    private Cliente sesionCliente;
    private Cuenta  cuentaActiva;

    public BancoService() {
        this.clienteRepo = new ClienteRepository();
        this.cuentaRepo  = new CuentaRepository();
    }

    // ── Acceso a repositorios ─────────────────────────────────
    public ClienteRepository getClienteRepo() { return clienteRepo; }
    public CuentaRepository  getCuentaRepo()  { return cuentaRepo; }

    // ════════════════════════════════════════════════════════════
    //  SESIÓN
    // ════════════════════════════════════════════════════════════

    public Cliente iniciarSesion(String username, String password) throws Exception {
        sesionCliente = clienteRepo.autenticar(username, password);
        // Seleccionar la primera cuenta como activa por defecto
        List<Cuenta> cuentas = cuentaRepo.obtenerPorPropietario(sesionCliente.getId());
        cuentaActiva = cuentas.isEmpty() ? null : cuentas.get(0);
        return sesionCliente;
    }

    public void cerrarSesion() {
        sesionCliente = null;
        cuentaActiva  = null;
    }

    public boolean estaAutenticado()    { return sesionCliente != null; }
    public Cliente getClienteActual()   { return sesionCliente; }
    public Cuenta  getCuentaActiva()    { return cuentaActiva; }

    public void seleccionarCuenta(String cuentaId) throws Exception {
        requiereAutenticacion();
        Cuenta c = cuentaRepo.obtenerPorId(cuentaId)
                .orElseThrow(() -> new Exception("Cuenta no encontrada: " + cuentaId));
        if (!c.getPropietarioId().equals(sesionCliente.getId()))
            throw new Exception("La cuenta no pertenece al usuario actual.");
        this.cuentaActiva = c;
    }

    public List<Cuenta> getCuentasCliente() {
        if (sesionCliente == null) return List.of();
        return cuentaRepo.obtenerPorPropietario(sesionCliente.getId());
    }

    // ════════════════════════════════════════════════════════════
    //  REGISTRO
    // ════════════════════════════════════════════════════════════

    public Cliente registrarCliente(String identificacion, String nombreCompleto,
                                    String celular, String username, String password,
                                    TipoCuenta tipoCuenta, double saldoOCupo) throws Exception {
        Cliente nuevo = clienteRepo.registrar(identificacion, nombreCompleto, celular, username, password);
        crearCuentaInicial(nuevo.getId(), tipoCuenta, saldoOCupo);
        return nuevo;
    }

    private void crearCuentaInicial(String userId, TipoCuenta tipo, double valor) {
        switch (tipo) {
            case AHORROS   -> cuentaRepo.crearAhorros(userId, valor);
            case CORRIENTE -> cuentaRepo.crearCorriente(userId, valor);
            case CREDITO   -> cuentaRepo.crearCredito(userId, valor);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TRANSACCIONES
    // ════════════════════════════════════════════════════════════

    public double consignar(double monto) throws Exception {
        requiereAutenticacion();
        return getCuentaActivaOError().consignar(monto);
    }

    public double retirar(double monto) throws Exception {
        requiereAutenticacion();
        return getCuentaActivaOError().retirar(monto);
    }

    public TarjetaCredito.ResultadoCompra realizarCompra(double monto, int cuotas) throws Exception {
        requiereAutenticacion();
        Cuenta cuenta = getCuentaActivaOError();
        if (!(cuenta instanceof TarjetaCredito tc))
            throw new Exception("La cuenta activa no es una Tarjeta de Crédito.");
        return tc.realizarCompra(monto, cuotas);
    }

    public double pagarTarjeta(double monto) throws Exception {
        requiereAutenticacion();
        Cuenta cuenta = getCuentaActivaOError();
        if (!(cuenta instanceof TarjetaCredito tc))
            throw new Exception("La cuenta activa no es una Tarjeta de Crédito.");
        return tc.pagarDeuda(monto);
    }

    public void transferir(String cuentaOrigenId, String cuentaDestinoRef, double monto) throws Exception {
        requiereAutenticacion();
        if (monto <= 0) throw new Exception("El monto de transferencia debe ser positivo.");

        Cuenta origen = cuentaRepo.obtenerPorId(cuentaOrigenId)
                .orElseThrow(() -> new Exception("Cuenta origen no encontrada."));

        if (!origen.getPropietarioId().equals(sesionCliente.getId()))
            throw new Exception("La cuenta origen no pertenece al usuario actual.");

        // Buscar destino por id o por número de cuenta
        Optional<Cuenta> optDestino = cuentaRepo.obtenerPorId(cuentaDestinoRef);
        if (optDestino.isEmpty())
            optDestino = cuentaRepo.obtenerPorNumeroCuenta(cuentaDestinoRef);
        Cuenta destino = optDestino.orElseThrow(() -> new Exception("Cuenta destino no encontrada."));

        if (origen.getId().equals(destino.getId()))
            throw new Exception("No se puede transferir a la misma cuenta.");
        if (origen.getTipo() == destino.getTipo() &&
            origen.getPropietarioId().equals(destino.getPropietarioId()))
            throw new Exception("No se permite transferir entre cuentas del mismo tipo del mismo usuario.");

        origen.retirar(monto);
        destino.consignar(monto);
    }

    // ════════════════════════════════════════════════════════════
    //  PERFIL
    // ════════════════════════════════════════════════════════════

    public void actualizarPerfil(String identificacion, String nombre, String celular) throws Exception {
        requiereAutenticacion();
        sesionCliente.actualizarPerfil(identificacion, nombre, celular);
    }

    public void cambiarPassword(String actual, String nueva, String confirmacion) throws Exception {
        requiereAutenticacion();
        sesionCliente.cambiarPassword(actual, nueva, confirmacion);
    }

    // ════════════════════════════════════════════════════════════
    //  ADMIN
    // ════════════════════════════════════════════════════════════

    public List<Cliente> obtenerTodosClientes() { return clienteRepo.obtenerTodos(); }

    public void eliminarCliente(String id) {
        cuentaRepo.eliminarPorPropietario(id);
        clienteRepo.eliminar(id);
    }

    public void desbloquearCliente(String id) throws Exception {
        Cliente c = clienteRepo.obtenerPorId(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado: " + id));
        c.desbloquear();
    }

    public void agregarCuentaACliente(String userId, TipoCuenta tipo, double valor) throws Exception {
        if (!clienteRepo.existePorId(userId))
            throw new Exception("Cliente no encontrado.");
        crearCuentaInicial(userId, tipo, valor);
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ════════════════════════════════════════════════════════════

    private void requiereAutenticacion() throws Exception {
        if (!estaAutenticado())
            throw new Exception("Debe iniciar sesión primero.");
    }

    private Cuenta getCuentaActivaOError() throws Exception {
        if (cuentaActiva == null)
            throw new Exception("No hay ninguna cuenta seleccionada.");
        return cuentaActiva;
    }
}
