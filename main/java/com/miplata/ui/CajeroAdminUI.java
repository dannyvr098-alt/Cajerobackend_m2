package com.miplata.ui;

import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MIP-18 — Registrar la recarga de efectivo en el cajero.
 * MIP-20 — Configurar los límites diarios de retiro por tipo de cliente.
 * MIP-21 — Colocar el cajero en modo mantenimiento.
 */
public class CajeroAdminUI {

    private final BancoService banco;

    private final Map<Integer, Integer> inventario = new LinkedHashMap<>();

    private double limiteEstandar    = 2_000_000;
    private double limitePremium     = 5_000_000;
    private double limiteEmpresarial = 20_000_000;

    private boolean       enMantenimiento     = false;
    private String        motivoMantenimiento  = "";
    private LocalDateTime inicioMantenimiento;

    public CajeroAdminUI(BancoService banco) {
        this.banco = banco;
        inventario.put(100_000, 50);
        inventario.put(50_000,  100);
        inventario.put(20_000,  150);
        inventario.put(10_000,  200);
    }

    public void mostrarMenuCajero() {
        boolean activo = true;
        while (activo) {
            Formato.titulo("ADMINISTRACIÓN DEL CAJERO");
            String estado = enMantenimiento ? "⚠  EN MANTENIMIENTO" : "✔  OPERATIVO";
            System.out.println("  Estado: " + estado);
            Formato.separador();
            System.out.println("  [1] Registrar recarga de efectivo   (MIP-18)");
            System.out.println("  [2] Configurar límites de retiro     (MIP-20)");
            System.out.println("  [3] Modo mantenimiento               (MIP-21)");
            System.out.println("  [4] Ver inventario actual");
            System.out.println("  [0] Volver");
            Formato.separador();
            switch (Consola.leerOpcion("Opción", 0, 4)) {
                case 1 -> registrarRecarga();
                case 2 -> configurarLimites();
                case 3 -> gestionarMantenimiento();
                case 4 -> verInventario();
                case 0 -> activo = false;
            }
        }
    }

    private void registrarRecarga() {
        Formato.titulo("MIP-18 | REGISTRAR RECARGA DE EFECTIVO");
        verInventario();
        Formato.separador();
        System.out.println("  Ingresa billetes cargados por denominación (0 si no aplica):");
        int[] denominaciones = {100_000, 50_000, 20_000, 10_000};
        Map<Integer, Integer> recarga = new LinkedHashMap<>();
        for (int den : denominaciones) {
            int cantidad = Consola.leerEntero("  Billetes de $" + String.format("%,d", den));
            if (cantidad < 0) { System.out.println("  ✗  No puede ser negativo."); Consola.pausar(); return; }
            recarga.put(den, cantidad);
        }
        double totalRecarga = 0;
        System.out.printf("%n  %-15s %-10s %-10s %-10s%n", "Denominación", "Anterior", "Añadido", "Nuevo");
        Formato.separador('-', 50);
        for (int den : denominaciones) {
            int anterior = inventario.getOrDefault(den, 0);
            int añadido  = recarga.getOrDefault(den, 0);
            totalRecarga += (double) añadido * den;
            System.out.printf("  $%-14s %-10d %-10d %-10d%n",
                    String.format("%,d", den), anterior, añadido, anterior + añadido);
        }
        System.out.println("  Total recargado: " + Formato.moneda(totalRecarga));
        if (Consola.confirmar("¿Confirmar recarga?")) {
            for (int den : denominaciones)
                inventario.merge(den, recarga.getOrDefault(den, 0), Integer::sum);
            System.out.println("  ✔  Recarga registrada. " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }
        Consola.pausar();
    }

    private void configurarLimites() {
        Formato.titulo("MIP-20 | LÍMITES DIARIOS DE RETIRO");
        System.out.println("  [1] Estándar:    " + Formato.moneda(limiteEstandar));
        System.out.println("  [2] Premium:     " + Formato.moneda(limitePremium));
        System.out.println("  [3] Empresarial: " + Formato.moneda(limiteEmpresarial));
        System.out.println("  [0] Volver");
        Formato.separador();
        switch (Consola.leerOpcion("Opción", 0, 3)) {
            case 1 -> limiteEstandar    = solicitarNuevoLimite("Estándar",    limiteEstandar);
            case 2 -> limitePremium     = solicitarNuevoLimite("Premium",     limitePremium);
            case 3 -> limiteEmpresarial = solicitarNuevoLimite("Empresarial", limiteEmpresarial);
            case 0 -> { return; }
        }
    }

    private double solicitarNuevoLimite(String tipo, double actual) {
        System.out.println("  Actual: " + Formato.moneda(actual));
        double nuevo = Consola.leerMonto("Nuevo límite para " + tipo);
        if (Consola.confirmar("¿Confirmar cambio?")) {
            System.out.println("  ✔  Límite actualizado.");
            Consola.pausar();
            return nuevo;
        }
        Consola.pausar();
        return actual;
    }

    private void gestionarMantenimiento() {
        Formato.titulo("MIP-21 | MODO MANTENIMIENTO");
        if (enMantenimiento) {
            System.out.println("  ⚠  Cajero EN MANTENIMIENTO.");
            System.out.println("  Motivo: " + motivoMantenimiento);
            System.out.println("  Desde:  " + inicioMantenimiento
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            if (Consola.confirmar("¿Finalizar mantenimiento?")) {
                enMantenimiento = false;
                System.out.println("  ✔  Cajero vuelto a operación normal.");
            }
        } else {
            System.out.println("  [1] Mantenimiento programado");
            System.out.println("  [2] Recarga de efectivo");
            System.out.println("  [3] Falla técnica");
            System.out.println("  [4] Actualización de software");
            System.out.println("  [0] Cancelar");
            int op = Consola.leerOpcion("Motivo", 0, 4);
            if (op == 0) { Consola.pausar(); return; }
            String[] motivos = {"", "Mantenimiento programado", "Recarga de efectivo",
                    "Falla técnica", "Actualización de software"};
            motivoMantenimiento = motivos[op];
            if (Consola.confirmar("¿Activar modo mantenimiento?")) {
                enMantenimiento     = true;
                inicioMantenimiento = LocalDateTime.now();
                System.out.println("  ✔  Cajero en modo mantenimiento.");
            }
        }
        Consola.pausar();
    }

    private void verInventario() {
        Formato.titulo("INVENTARIO DE EFECTIVO");
        System.out.printf("  %-20s %-12s %-15s%n", "Denominación", "Billetes", "Subtotal");
        Formato.separador('-', 50);
        double total = 0;
        for (Map.Entry<Integer, Integer> e : inventario.entrySet()) {
            double subtotal = (double) e.getKey() * e.getValue();
            total += subtotal;
            System.out.printf("  $%-19s %-12d %s%n",
                    String.format("%,d", e.getKey()), e.getValue(), Formato.moneda(subtotal));
        }
        Formato.separador('-', 50);
        System.out.println("  Total: " + Formato.moneda(total));
        Consola.pausar();
    }

    public boolean isEnMantenimiento() { return enMantenimiento; }
}