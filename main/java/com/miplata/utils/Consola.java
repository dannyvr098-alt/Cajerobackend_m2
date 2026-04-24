package com.miplata.utils;

import java.util.Scanner;

/**
 * Wrapper para lectura de consola con validaciones de entrada.
 */
public class Consola {

    private static final Scanner scanner = new Scanner(System.in);

    public static String leerTexto(String prompt) {
        System.out.print("  » " + prompt + ": ");
        return scanner.nextLine().trim();
    }

    public static String leerPassword(String prompt) {
        System.out.print("  » " + prompt + ": ");
        // En consola real se usa Console.readPassword(); aquí simplificamos
        return scanner.nextLine().trim();
    }

    public static double leerMonto(String prompt) {
        while (true) {
            System.out.print("  » " + prompt + ": ");
            String input = scanner.nextLine().trim();
            try {
                double valor = Double.parseDouble(input.replace(",", "").replace("$", ""));
                if (valor <= 0) {
                    System.out.println("  ✗ El monto debe ser mayor a cero.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Ingresa un número válido.");
            }
        }
    }

    public static int leerEntero(String prompt) {
        while (true) {
            System.out.print("  » " + prompt + ": ");
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Ingresa un número entero válido.");
            }
        }
    }

    public static int leerOpcion(String prompt, int min, int max) {
        while (true) {
            int opcion = leerEntero(prompt);
            if (opcion >= min && opcion <= max) return opcion;
            System.out.printf("  ✗ Opción inválida. Elige entre %d y %d.%n", min, max);
        }
    }

    public static boolean confirmar(String prompt) {
        System.out.print("  » " + prompt + " (s/n): ");
        String resp = scanner.nextLine().trim().toLowerCase();
        return resp.equals("s") || resp.equals("si") || resp.equals("sí");
    }

    public static void pausar() {
        System.out.print("\n  Presiona ENTER para continuar...");
        scanner.nextLine();
    }
}
