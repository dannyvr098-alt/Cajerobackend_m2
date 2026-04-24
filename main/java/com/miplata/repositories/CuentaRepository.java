package com.miplata.repositories;

import com.miplata.enums.TipoCuenta;
import com.miplata.models.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CuentaRepository implements IRepository<Cuenta, String> {

    // Lista interna — guarda todos los tipos de cuenta polimórficamente
    private final List<Cuenta> lista = new ArrayList<>();

    //  CRUD COMPLETO (IRepository)

    @Override
    public Cuenta agregar(Cuenta cuenta) {
        if (cuenta == null)
            throw new IllegalArgumentException("La cuenta no puede ser nula.");
        if (existePorId(cuenta.getId()))
            throw new IllegalStateException("Ya existe una cuenta con id: " + cuenta.getId());

        lista.add(cuenta);
        return cuenta;
    }

    @Override
    public Optional<Cuenta> obtenerPorId(String id) {
        return lista.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Cuenta> obtenerTodos() {
        return Collections.unmodifiableList(new ArrayList<>(lista));
    }

    @Override
    public Cuenta actualizar(Cuenta cuentaActualizada) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(cuentaActualizada.getId())) {
                lista.set(i, cuentaActualizada);
                return cuentaActualizada;
            }
        }
        throw new IllegalArgumentException("Cuenta con id " + cuentaActualizada.getId() + " no encontrada.");
    }

    @Override
    public boolean eliminar(String id) {
        boolean eliminado = lista.removeIf(c -> c.getId().equals(id));
        if (!eliminado)
            throw new IllegalArgumentException("Cuenta con id " + id + " no encontrada.");
        return true;
    }

    @Override
    public int contar() { return lista.size(); }

    //  BÚSQUEDAS DE DOMINIO


    public List<Cuenta> obtenerPorPropietario(String userId) {
        return lista.stream()
                .filter(c -> c.getPropietarioId().equals(userId))
                .collect(Collectors.toList());
    }

    public Optional<Cuenta> obtenerPorNumeroCuenta(String numeroCuenta) {
        return lista.stream()
                .filter(c -> c.getNumeroCuenta().equals(numeroCuenta))
                .findFirst();
    }

    public Optional<Cuenta> obtenerPorPropietarioYTipo(String userId, TipoCuenta tipo) {
        return lista.stream()
                .filter(c -> c.getPropietarioId().equals(userId) && c.getTipo() == tipo)
                .findFirst();
    }

    public boolean existePorId(String id) {
        return lista.stream().anyMatch(c -> c.getId().equals(id));
    }

    public void eliminarPorPropietario(String userId) {
        lista.removeIf(c -> c.getPropietarioId().equals(userId));
    }

    //  FÁBRICA DE CUENTAS


    public CuentaAhorros crearAhorros(String userId, double saldoInicial) {
        CuentaAhorros cuenta = new CuentaAhorros(userId, saldoInicial);
        agregar(cuenta);
        return cuenta;
    }

    public CuentaCorriente crearCorriente(String userId, double saldoInicial) {
        CuentaCorriente cuenta = new CuentaCorriente(userId, saldoInicial);
        agregar(cuenta);
        return cuenta;
    }

    public TarjetaCredito crearCredito(String userId, double cupo) {
        TarjetaCredito cuenta = new TarjetaCredito(userId, cupo);
        agregar(cuenta);
        return cuenta;
    }
}
