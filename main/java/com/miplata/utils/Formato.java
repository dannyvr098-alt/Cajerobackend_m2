package com.miplata.utils;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilidades de formato para la interfaz de consola.
 */
public class Formato {

    private static final NumberFormat    MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String moneda(double valor) {
        return MONEDA.format(valor);
    }

    public static String fecha(LocalDateTime ldt) {
        return ldt.format(FECHA);
    }

    /** Imprime una línea separadora con el carácter y ancho dados. */
    public static void separador(char caracter, int ancho) {
        System.out.println(String.valueOf(caracter).repeat(ancho));
    }

    public static void separador() { separador('─', 60); }

    /** Imprime un título centrado con separadores. */
    public static void titulo(String texto) {
        separador('═', 60);
        int padding = (60 - texto.length()) / 2;
        System.out.println(" ".repeat(Math.max(0, padding)) + texto);
        separador('═', 60);
    }

    public static void subtitulo(String texto) {
        System.out.println();
        System.out.println("  ┌─ " + texto + " ─┐");
    }
}
