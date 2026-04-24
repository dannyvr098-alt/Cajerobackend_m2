package com.miplata.ui;

import com.miplata.enums.TipoCuenta;
import com.miplata.models.Cliente;
import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

public class AuthUI {

    private final BancoService banco;

    public AuthUI(BancoService banco) {
        this.banco = banco;
    }

    //  LANDING — menú principal de bienvenida

    public void mostrarLanding() {
        Formato.titulo("MI PLATA");
        System.out.println();
        System.out.println("       Bienvenido al Sistema Bancario Mi Plata");
        Formato.separador();
        System.out.println("  [1] Iniciar Sesión");
        System.out.println("  [2] Registrarse");
        System.out.println("  [0] Salir");
        Formato.separador();
    }


    //  LOGIN
    public boolean mostrarLogin() {
        Formato.titulo("INICIAR SESIÓN");
        int intentosUI = 0;

        while (true) {
            if (intentosUI > 0) {
                System.out.printf("  ⚠  Intentos fallidos: %d/3%n", intentosUI);
                mostrarPuntos(intentosUI);
            }

            String username = Consola.leerTexto("Usuario");
            String password = Consola.leerPassword("Contraseña");

            try {
                banco.iniciarSesion(username, password);
                System.out.println("\n  ✔  ¡Bienvenido, " + banco.getClienteActual().getNombreCompleto() + "!");
                Consola.pausar();
                return true;
            } catch (Exception e) {
                intentosUI++;
                System.out.println("\n  ✗  " + e.getMessage());

                if (intentosUI >= 3 || e.getMessage().contains("bloqueada")) {
                    System.out.println("  🔒 Cuenta bloqueada. Contacte al administrador.");
                    Consola.pausar();
                    return false;
                }

                System.out.println();
            }
        }
    }

    /** Muestra los puntos indicadores de intentos (● activo ○ vacío). */
    private void mostrarPuntos(int intentos) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 1; i <= 3; i++) sb.append(i <= intentos ? "● " : "○ ");
        System.out.println(sb);
    }

    // ════════════════════════════════════════════════════════════
    //  REGISTRO
    // ════════════════════════════════════════════════════════════
    public void mostrarRegistro() {
        Formato.titulo("CREAR NUEVA CUENTA");

        while (true) {
            try {
                String identificacion = Consola.leerTexto("Número de identificación (cédula/pasaporte)");
                String nombre         = Consola.leerTexto("Nombre completo");
                String celular        = Consola.leerTexto("Número de celular (10 dígitos)");
                String username       = Consola.leerTexto("Nombre de usuario (mín. 4 chars, solo letras/números/_)");
                String password       = Consola.leerPassword("Contraseña (mín. 6 chars, 1 mayúscula, 1 número)");
                String confirmacion   = Consola.leerPassword("Confirmar contraseña");

                if (!password.equals(confirmacion)) {
                    System.out.println("  ✗  Las contraseñas no coinciden. Intenta de nuevo.\n");
                    continue;
                }

                System.out.println();
                System.out.println("  Tipo de cuenta inicial:");
                System.out.println("  [1] Cuenta de Ahorros");
                System.out.println("  [2] Cuenta Corriente");
                System.out.println("  [3] Tarjeta de Crédito");
                int opTipo = Consola.leerOpcion("Elige una opción", 1, 3);

                TipoCuenta tipo;
                double valorInicial = 0;

                switch (opTipo) {
                    case 1 -> {
                        tipo = TipoCuenta.AHORROS;
                        valorInicial = Consola.leerMonto("Saldo inicial (puede ser 0)");
                    }
                    case 2 -> {
                        tipo = TipoCuenta.CORRIENTE;
                        valorInicial = Consola.leerMonto("Saldo inicial (puede ser 0)");
                    }
                    default -> {
                        tipo = TipoCuenta.CREDITO;
                        valorInicial = Consola.leerMonto("Cupo de crédito asignado (mín. 100000)");
                        if (valorInicial < 100000) {
                            System.out.println("  ✗  El cupo mínimo es $100.000.");
                            continue;
                        }
                    }
                }

                Cliente nuevo = banco.registrarCliente(
                        identificacion, nombre, celular, username, password, tipo, valorInicial);

                System.out.println("\n  ✔  ¡Cuenta creada exitosamente!");
                System.out.println("  Cliente: " + nuevo.getNombreCompleto());
                System.out.println("  Usuario: @" + nuevo.getUsername());
                Consola.pausar();
                return;

            } catch (Exception e) {  // ✅ CORRECCIÓN: se eliminó IllegalArgumentException
                System.out.println("\n  ✗  " + e.getMessage() + "\n");
            }
        }
    }
}