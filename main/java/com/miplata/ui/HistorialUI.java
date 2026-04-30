package com.miplata.ui;

import com.miplata.models.Cliente;
import com.miplata.models.Cuenta;
import com.miplata.models.Transaccion;
import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

import java.util.List;

/**
 * MIP-5 — Ver el historial completo de transacciones de un cliente.
 */
public class HistorialUI {

    private final BancoService banco;

    public HistorialUI(BancoService banco) {
        this.banco = banco;
    }

    public void mostrarHistorialAdmin() {
        Formato.titulo("MIP-5 | HISTORIAL DE TRANSACCIONES");
        List<Cliente> todos = banco.obtenerTodosClientes();
        if (todos.isEmpty()) {
            System.out.println("  No hay clientes registrados.");
            Consola.pausar();
            return;
        }
        System.out.println("  Selecciona un cliente:");
        for (int i = 0; i < todos.size(); i++) {
            Cliente c = todos.get(i);
            System.out.printf("  [%d] %s (@%s) — %s%n",
                    i + 1, c.getNombreCompleto(), c.getUsername(),
                    c.getEstado().getDescripcion());
        }
        int idx = Consola.leerOpcion("Opción", 1, todos.size()) - 1;
        mostrarTransaccionesCliente(todos.get(idx));
    }

    private void mostrarTransaccionesCliente(Cliente cliente) {
        Formato.titulo("HISTORIAL — " + cliente.getNombreCompleto().toUpperCase());
        System.out.println("  Usuario:        @" + cliente.getUsername());
        System.out.println("  Identificación: " + cliente.getIdentificacion());
        System.out.println("  Estado:         " + cliente.getEstado().getDescripcion());
        Formato.separador();
        List<Cuenta> cuentas = banco.getCuentaRepo().obtenerPorPropietario(cliente.getId());
        if (cuentas.isEmpty()) {
            System.out.println("  Este cliente no tiene cuentas registradas.");
            Consola.pausar();
            return;
        }
        int totalMovimientos = 0;
        for (Cuenta cuenta : cuentas) {
            System.out.println("\n  ► " + cuenta.getTipo().getDescripcion()
                    + " — N° " + cuenta.getNumeroCuenta()
                    + " | Estado: " + cuenta.getEstado().getDescripcion());
            Formato.separador('-', 72);
            List<Transaccion> movs = cuenta.getMovimientos();
            if (movs.isEmpty()) {
                System.out.println("    Sin movimientos en esta cuenta.");
            } else {
                System.out.printf("    %-10s %-22s %-20s %-14s %s%n",
                        "ID", "Fecha", "Tipo", "Valor", "Saldo resultante");
                Formato.separador('-', 72);
                for (Transaccion t : movs) {
                    System.out.printf("    %-10s %-22s %-20s %-14s %s%n",
                            t.getId(), Formato.fecha(t.getFecha()),
                            t.getTipo().getDescripcion(),
                            Formato.moneda(t.getValor()),
                            Formato.moneda(t.getSaldoResultante()));
                    System.out.printf("               Detalle: %s%n", t.getDescripcion());
                    Formato.separador('-', 72);
                    totalMovimientos++;
                }
                System.out.println("    Subtotal: " + movs.size() + " movimiento(s).");
            }
        }
        Formato.separador();
        System.out.println("  Total general de movimientos: " + totalMovimientos);
        Consola.pausar();
    }
}