package com.miplata.repositories;

import com.miplata.models.Cliente;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class ClienteRepository implements IRepository<Cliente, String> {

    // Lista interna — equivale a la "base de datos" en memoria
    private final List<Cliente> lista = new ArrayList<>();


    //  CRUD COMPLETO (IRepository)

    @Override
    public Cliente agregar(Cliente cliente) {
        if (cliente == null)
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        if (existePorId(cliente.getId()))
            throw new IllegalStateException("Ya existe un cliente con id: " + cliente.getId());

        lista.add(cliente);
        return cliente;
    }

    @Override
    public Optional<Cliente> obtenerPorId(String id) {
        return lista.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Cliente> obtenerTodos() {
        return Collections.unmodifiableList(new ArrayList<>(lista));
    }

    @Override
    public Cliente actualizar(Cliente clienteActualizado) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(clienteActualizado.getId())) {
                lista.set(i, clienteActualizado);
                return clienteActualizado;
            }
        }
        throw new IllegalArgumentException("Cliente con id " + clienteActualizado.getId() + " no encontrado.");
    }

    @Override
    public boolean eliminar(String id) {
        boolean eliminado = lista.removeIf(c -> c.getId().equals(id));
        if (!eliminado)
            throw new IllegalArgumentException("Cliente con id " + id + " no encontrado.");
        return true;
    }

    @Override
    public int contar() { return lista.size(); }

    //  BÚSQUEDAS DE DOMINIO

    public Optional<Cliente> obtenerPorUsername(String username) {
        return lista.stream()
                .filter(c -> c.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public boolean existeUsername(String username) {
        return lista.stream()
                .anyMatch(c -> c.getUsername().equalsIgnoreCase(username));
    }

    public boolean existeIdentificacion(String identificacion) {
        return lista.stream()
                .anyMatch(c -> c.getIdentificacion().equals(identificacion));
    }

    public boolean existePorId(String id) {
        return lista.stream().anyMatch(c -> c.getId().equals(id));
    }

    public List<Cliente> obtenerBloqueados() {
        return lista.stream()
                .filter(Cliente::estaBloqueado)
                .collect(Collectors.toList());
    }

    //  AUTENTICACIÓN


    /**
     * Autentica un cliente por username y contraseña.
     * Gestiona el contador de intentos fallidos y el bloqueo.
     */
    public Cliente autenticar(String username, String password) throws Exception {
        Cliente cliente = obtenerPorUsername(username)
                .orElseThrow(() -> new Exception("Usuario no encontrado: " + username));

        if (cliente.estaBloqueado())
            throw new Exception("Cuenta bloqueada. Contacte al administrador.");

        if (!cliente.verificarPassword(password)) {
            int intentos = cliente.registrarIntentoFallido();
            if (cliente.estaBloqueado())
                throw new Exception("Cuenta bloqueada por demasiados intentos fallidos (3/3).");
            throw new Exception("Contraseña incorrecta. Intentos fallidos: " + intentos + "/3.");
        }

        cliente.resetearIntentos();
        return cliente;
    }

    /**
     * Registra un nuevo cliente validando unicidad de username e identificación.
     */
    public Cliente registrar(String identificacion, String nombreCompleto,
                             String celular, String username, String password) throws Exception {
        if (existeUsername(username))
            throw new Exception("El nombre de usuario \"" + username + "\" ya está en uso.");
        if (existeIdentificacion(identificacion))
            throw new Exception("Ya existe un cliente con la identificación " + identificacion + ".");

        Cliente nuevo = new Cliente(identificacion, nombreCompleto, celular, username, password);
        return agregar(nuevo);
    }
}
