package com.miplata.ui;

import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

/**
 * MIP-9  — Cambiar mi PIN desde el cajero.
 * MIP-10 — Configurar el número máximo de intentos fallidos y el tiempo de bloqueo.
 */
public class SeguridadUI {

    private final BancoService banco;

    private static int maxIntentos      = 3;
    private static int tiempoBloqueoMin = 30;

    public SeguridadUI(BancoService banco) {
        this.banco = banco;
    }

    public void cambiarPin() {
        Formato.titulo("MIP-9 | CAMBIAR PIN");
        if (!banco.estaAutenticado()) {
            System.out.println("  ✗  Debe iniciar sesión primero.");
            Consola.pausar();
            return;
        }
        System.out.println("  Usuario: @" + banco.getClienteActual().getUsername());
        Formato.separador();
        try {
            String pinActual = Consola.leerPassword("PIN actual");
            String pinNuevo  = Consola.leerPassword("Nuevo PIN (mín. 6 chars, 1 mayúscula, 1 número)");
            String confirmar = Consola.leerPassword("Confirmar nuevo PIN");
            banco.cambiarPassword(pinActual, pinNuevo, confirmar);
            System.out.println("\n  ✔  PIN cambiado exitosamente.");
        } catch (Exception e) {
            System.out.println("\n  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    public void configurarPoliticaSeguridad() {
        Formato.titulo("MIP-10 | CONFIGURAR POLÍTICA DE SEGURIDAD");
        System.out.println("  Configuración actual:");
        System.out.println("  ├─ Intentos máximos: " + maxIntentos);
        System.out.println("  └─ Tiempo de bloqueo: " + tiempoBloqueoMin + " minutos");
        Formato.separador();

        boolean activo = true;
        while (activo) {
            System.out.println("  [1] Cambiar número máximo de intentos");
            System.out.println("  [2] Cambiar tiempo de bloqueo");
            System.out.println("  [3] Ver configuración actual");
            System.out.println("  [0] Volver");
            Formato.separador();
            switch (Consola.leerOpcion("Opción", 0, 3)) {
                case 1 -> configurarMaxIntentos();
                case 2 -> configurarTiempoBloqueo();
                case 3 -> mostrarConfiguracion();
                case 0 -> activo = false;
            }
        }
    }

    private void configurarMaxIntentos() {
        System.out.println("  Valor actual: " + maxIntentos + " | Rango: 3 a 10");
        int nuevo = Consola.leerEntero("Nuevo valor");
        if (nuevo < 3 || nuevo > 10) {
            System.out.println("  ✗  Valor fuera del rango (3-10).");
            Consola.pausar(); return;
        }
        if (Consola.confirmar("¿Confirmar cambio de " + maxIntentos + " a " + nuevo + "?")) {
            maxIntentos = nuevo;
            System.out.println("  ✔  Configuración actualizada.");
        }
        Consola.pausar();
    }

    private void configurarTiempoBloqueo() {
        System.out.println("  Valor actual: " + tiempoBloqueoMin + " min | Rango: 5 a 1440");
        int nuevo = Consola.leerEntero("Nuevo valor (minutos)");
        if (nuevo < 5 || nuevo > 1440) {
            System.out.println("  ✗  Valor fuera del rango (5-1440).");
            Consola.pausar(); return;
        }
        if (Consola.confirmar("¿Confirmar cambio de " + tiempoBloqueoMin + " a " + nuevo + " min?")) {
            tiempoBloqueoMin = nuevo;
            System.out.println("  ✔  Configuración actualizada.");
        }
        Consola.pausar();
    }

    private void mostrarConfiguracion() {
        Formato.titulo("CONFIGURACIÓN ACTUAL");
        System.out.println("  Intentos máximos: " + maxIntentos);
        System.out.println("  Tiempo de bloqueo: " + tiempoBloqueoMin + " minutos");
        Consola.pausar();
    }

    public static int getMaxIntentos()      { return maxIntentos; }
    public static int getTiempoBloqueoMin() { return tiempoBloqueoMin; }
}