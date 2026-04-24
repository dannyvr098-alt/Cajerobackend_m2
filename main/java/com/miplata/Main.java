package com.miplata;

import com.miplata.enums.TipoCuenta;
import com.miplata.services.BancoService;
import com.miplata.ui.AuthUI;
import com.miplata.ui.TransaccionesUI;
import com.miplata.utils.Formato;


public class Main {

    public static void main(String[] args) {

        BancoService banco = new BancoService();
        cargarDatosDePrueba(banco);

        AuthUI         authUI = new AuthUI(banco);
        TransaccionesUI cajero = new TransaccionesUI(banco);

        boolean ejecutando = true;
        while (ejecutando) {
            authUI.mostrarLanding();
            int opcion = leerOpcionLanding();

            switch (opcion) {
                case 1 -> {
                    boolean loginOk = authUI.mostrarLogin();
                    if (loginOk) cajero.mostrarPanel();
                }
                case 2 -> authUI.mostrarRegistro();
                case 0 -> {
                    Formato.titulo("HASTA PRONTO — MI PLATA");
                    System.out.println("  Gracias por usar Mi Plata Financial Bank.");
                    System.out.println("  © 2025 CESDE · Desarrollo de Software\n");
                    ejecutando = false;
                }
            }
        }
    }

    // Lectura segura de la opción del landing
    private static int leerOpcionLanding() {
        while (true) {
            System.out.print("  » Elige una opción: ");
            try {
                String line = new java.util.Scanner(System.in).nextLine().trim();
                int op = Integer.parseInt(line);
                if (op >= 0 && op <= 2) return op;
                System.out.println("  ✗  Opción inválida. Elige 0, 1 o 2.\n");
            } catch (NumberFormatException e) {
                System.out.println("  ✗  Ingresa un número válido.\n");
            }
        }
    }


    //  DATOS DE PRUEBA (pusimos un ejemplo)

    private static void cargarDatosDePrueba(BancoService banco) {
        try {
            // Admin / docente
            banco.registrarCliente(
                    "1000000001", "Jorge Muriel Admin",
                    "3001234567", "admin", "Admin123",
                    TipoCuenta.CORRIENTE, 5_000_000);

            banco.registrarCliente(
                    "1012345678", "Ana María López",
                    "3109876543", "analopez", "Ana2025",
                    TipoCuenta.AHORROS, 2_500_000);

            // Agregar tarjeta de crédito a Ana
            banco.agregarCuentaACliente(
                    banco.getClienteRepo()
                         .obtenerPorUsername("analopez").get().getId(),
                    TipoCuenta.CREDITO, 8_000_000);

            // Cliente 2: Carlos (Corriente)
            banco.registrarCliente(
                    "1023456789", "Carlos Pérez",
                    "3201234567", "carlosperez", "Carlos1",
                    TipoCuenta.CORRIENTE, 1_800_000);

            System.out.println("\n  ✔  Datos de prueba cargados.");
            System.out.println("  ┌──────────────────────────────────────┐");
            System.out.println("  │  Usuarios de prueba disponibles:     │");
            System.out.println("  │  Usuario: admin       Clave: Admin123│");
            System.out.println("  │  Usuario: analopez    Clave: Ana2025 │");
            System.out.println("  │  Usuario: carlosperez Clave: Carlos1 │");
            System.out.println("  └──────────────────────────────────────┘\n");

        } catch (Exception e) {
            // Silencioso si los datos ya existen
        }
    }
}
