package com.miplata.ui;

import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MIP-27 — Cancelar cualquier operación antes de confirmarla.
 * MIP-28 — Registrar en log cada error técnico con código y timestamp.
 * MIP-29 — Reversar una transacción fallida que descontó saldo.
 * MIP-30 — Visualizar y gestionar los incidentes abiertos de un cajero.
 */
public class SoporteUI {

    private final BancoService banco;

    private static final List<String[]> LOG_ERRORES         = new ArrayList<>();
    private static final List<String[]> REVERSIONES         = new ArrayList<>();
    private static final List<String[]> INCIDENTES_ABIERTOS = new ArrayList<>();

    public SoporteUI(BancoService banco) {
        this.banco = banco;
        cargarDemos();
    }

    public void mostrarMenuSoporte() {
        boolean activo = true;
        while (activo) {
            Formato.titulo("SOPORTE Y ERRORES");
            System.out.println("  [1] Cancelar operación en curso        (MIP-27)");
            System.out.println("  [2] Ver log de errores técnicos        (MIP-28)");
            System.out.println("  [3] Gestionar reversiones              (MIP-29)");
            System.out.println("  [4] Gestionar incidentes del cajero    (MIP-30)");
            System.out.println("  [0] Volver");
            Formato.separador();
            switch (Consola.leerOpcion("Opción", 0, 4)) {
                case 1 -> cancelarOperacion();
                case 2 -> verLogErrores();
                case 3 -> gestionarReversiones();
                case 4 -> gestionarIncidentes();
                case 0 -> activo = false;
            }
        }
    }

    // MIP-27
    public boolean solicitarCancelacion(String nombreOperacion) {
        Formato.separador();
        System.out.println("  ⚠  ¿Deseas cancelar: " + nombreOperacion + "?");
        System.out.println("  [1] Sí, cancelar");
        System.out.println("  [2] No, continuar");
        Formato.separador();
        if (Consola.leerOpcion("Opción", 1, 2) == 1) {
            System.out.println("  ✔  Operación cancelada. Ningún movimiento fue procesado.");
            registrarLog("MIP27-001", "INFO", "Operación cancelada: " + nombreOperacion);
            Consola.pausar();
            return true;
        }
        return false;
    }

    private void cancelarOperacion() {
        Formato.titulo("MIP-27 | CANCELAR OPERACIÓN");
        System.out.println("  [1] Retiro de efectivo");
        System.out.println("  [2] Transferencia");
        System.out.println("  [3] Pago de servicio");
        System.out.println("  [0] Volver");
        Formato.separador();
        int op = Consola.leerOpcion("Opción", 0, 3);
        if (op == 0) return;
        String[] ops = {"", "Retiro de efectivo", "Transferencia", "Pago de servicio"};
        solicitarCancelacion(ops[op]);
    }

    // MIP-28
    public static void registrarLog(String codigo, String severidad, String descripcion) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS"));
        LOG_ERRORES.add(new String[]{timestamp, codigo, severidad, descripcion});
    }

    private void verLogErrores() {
        Formato.titulo("MIP-28 | LOG DE ERRORES TÉCNICOS");
        System.out.println("  [1] Todos  [2] Solo CRITICO y ALTO  [3] Solo MEDIO y BAJO");
        Formato.separador();
        int filtro = Consola.leerOpcion("Filtro", 1, 3);
        System.out.printf("  %-26s %-12s %-8s %s%n", "Timestamp", "Código", "Nivel", "Descripción");
        Formato.separador('-', 80);
        int count = 0;
        for (String[] e : LOG_ERRORES) {
            boolean mostrar = switch (filtro) {
                case 2 -> e[2].equals("CRITICO") || e[2].equals("ALTO");
                case 3 -> e[2].equals("MEDIO")   || e[2].equals("BAJO");
                default -> true;
            };
            if (mostrar) {
                System.out.printf("  %-26s %-12s %-8s %s%n", e[0], e[1], e[2], e[3]);
                count++;
            }
        }
        Formato.separador('-', 80);
        System.out.println("  Registros mostrados: " + count + " | Total en log: " + LOG_ERRORES.size());
        Consola.pausar();
    }

    // MIP-29
    public static void registrarReversionPendiente(String ref, String cliente, double monto) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        REVERSIONES.add(new String[]{ref, timestamp, cliente,
                String.format("$%,.2f", monto), "PENDIENTE"});
        registrarLog("MIP29-001", "ALTO", "Reversión pendiente: " + ref + " — " + cliente);
    }

    private void gestionarReversiones() {
        Formato.titulo("MIP-29 | REVERSIONES");
        if (REVERSIONES.isEmpty()) {
            System.out.println("  ✔  No hay reversiones pendientes.");
            Consola.pausar(); return;
        }
        System.out.printf("  %-12s %-22s %-20s %-14s %-12s%n",
                "Ref.", "Fecha", "Cliente", "Monto", "Estado");
        Formato.separador('-', 82);
        for (int i = 0; i < REVERSIONES.size(); i++) {
            String[] r = REVERSIONES.get(i);
            System.out.printf("  [%d] %-10s %-22s %-20s %-14s %-12s%n",
                    i + 1, r[0], r[1], r[2], r[3], r[4]);
        }
        Formato.separador('-', 82);
        System.out.println("  [1] Procesar reversión  [0] Volver");
        if (Consola.leerOpcion("Opción", 0, 1) == 1) {
            int idx = Consola.leerOpcion("Selecciona", 1, REVERSIONES.size()) - 1;
            if (Consola.confirmar("¿Confirmar reversión " + REVERSIONES.get(idx)[0] + "?")) {
                REVERSIONES.get(idx)[4] = "REVERTIDA";
                registrarLog("MIP29-REV", "INFO", "Reversión procesada: " + REVERSIONES.get(idx)[0]);
                System.out.println("  ✔  Reversión procesada. Saldo restituido.");
            }
        }
        Consola.pausar();
    }

    // MIP-30
    public static void registrarIncidente(String tipo, String nivel) {
        String id        = "INC-" + String.format("%03d", INCIDENTES_ABIERTOS.size() + 1);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        INCIDENTES_ABIERTOS.add(new String[]{id, timestamp, tipo, nivel, "ABIERTO"});
        registrarLog("MIP30-NEW", nivel, "Nuevo incidente: " + tipo);
    }

    private void gestionarIncidentes() {
        Formato.titulo("MIP-30 | INCIDENTES DEL CAJERO");
        if (INCIDENTES_ABIERTOS.isEmpty()) {
            System.out.println("  ✔  No hay incidentes abiertos.");
            Consola.pausar(); return;
        }
        System.out.printf("  %-10s %-22s %-25s %-8s %-12s%n",
                "ID", "Apertura", "Tipo", "Nivel", "Estado");
        Formato.separador('-', 80);
        for (int i = 0; i < INCIDENTES_ABIERTOS.size(); i++) {
            String[] inc = INCIDENTES_ABIERTOS.get(i);
            System.out.printf("  [%d] %-8s %-22s %-25s %-8s %-12s%n",
                    i + 1, inc[0], inc[1], inc[2], inc[3], inc[4]);
        }
        Formato.separador('-', 80);
        System.out.println("  [1] Cerrar incidente  [2] Agregar nota  [0] Volver");
        switch (Consola.leerOpcion("Opción", 0, 2)) {
            case 1 -> cerrarIncidente();
            case 2 -> agregarNota();
            case 0 -> { return; }
        }
        Consola.pausar();
    }

    private void cerrarIncidente() {
        if (INCIDENTES_ABIERTOS.isEmpty()) return;
        int idx       = Consola.leerOpcion("Selecciona incidente", 1, INCIDENTES_ABIERTOS.size()) - 1;
        String causa  = Consola.leerTexto("Causa raíz");
        String accion = Consola.leerTexto("Acción correctiva");
        INCIDENTES_ABIERTOS.get(idx)[4] = "CERRADO";
        registrarLog("MIP30-CLS", "INFO", "Incidente cerrado: " + INCIDENTES_ABIERTOS.get(idx)[0]
                + " | Causa: " + causa + " | Acción: " + accion);
        System.out.println("  ✔  Incidente cerrado.");
    }

    private void agregarNota() {
        if (INCIDENTES_ABIERTOS.isEmpty()) return;
        int idx     = Consola.leerOpcion("Selecciona incidente", 1, INCIDENTES_ABIERTOS.size()) - 1;
        String nota = Consola.leerTexto("Nota de seguimiento");
        registrarLog("MIP30-NTA", "BAJO", "Nota en " + INCIDENTES_ABIERTOS.get(idx)[0] + ": " + nota);
        System.out.println("  ✔  Nota registrada.");
    }

    private void cargarDemos() {
        if (LOG_ERRORES.isEmpty()) {
            registrarLog("ATM-001",  "MEDIO",   "Impresora sin papel");
            registrarLog("CORE-042", "ALTO",    "Timeout en core bancario");
            registrarLog("PAY-017",  "BAJO",    "Reintento switch de recaudo");
            registrarLog("ATM-005",  "CRITICO", "Falla en dispensador de efectivo");
        }
        if (INCIDENTES_ABIERTOS.isEmpty()) {
            registrarIncidente("Falla en dispensador", "CRITICO");
            registrarIncidente("Impresora sin papel",  "BAJO");
        }
    }
}