package com.miplata.ui;

import com.miplata.models.Cliente;
import com.miplata.models.Cuenta;
import com.miplata.models.Transaccion;
import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MIP-23 — Exportar el reporte de todas las transacciones del día/semana/mes.
 * MIP-24 — Ver un listado de transacciones marcadas como inusuales (sospechosas).
 * MIP-25 — Ver el uptime y los incidentes registrados del cajero.
 */
public class ReportesUI {

    private final BancoService banco;

    private static final String[][] INCIDENTES = {
            {"INC-001", "Falla de red",           "01/06/2025 08:15", "01/06/2025 09:00", "RESUELTO"},
            {"INC-002", "Sin papel impresora",    "03/06/2025 14:30", "03/06/2025 14:45", "RESUELTO"},
            {"INC-003", "Bajo nivel de efectivo", "05/06/2025 11:00", "05/06/2025 12:30", "RESUELTO"},
            {"INC-004", "Error dispensador",      "08/06/2025 09:20", "—",                "ABIERTO"},
    };

    public ReportesUI(BancoService banco) {
        this.banco = banco;
    }

    public void mostrarMenuReportes() {
        boolean activo = true;
        while (activo) {
            Formato.titulo("REPORTES Y AUDITORÍA");
            System.out.println("  [1] Exportar reporte de transacciones   (MIP-23)");
            System.out.println("  [2] Transacciones sospechosas           (MIP-24)");
            System.out.println("  [3] Uptime e incidentes del cajero      (MIP-25)");
            System.out.println("  [0] Volver");
            Formato.separador();
            switch (Consola.leerOpcion("Opción", 0, 3)) {
                case 1 -> exportarReporte();
                case 2 -> verSospechosas();
                case 3 -> verUptime();
                case 0 -> activo = false;
            }
        }
    }

    private void exportarReporte() {
        Formato.titulo("MIP-23 | REPORTE DE TRANSACCIONES");
        System.out.println("  [1] Hoy");
        System.out.println("  [2] Última semana");
        System.out.println("  [3] Último mes");
        Formato.separador();
        int op = Consola.leerOpcion("Período", 1, 3);
        LocalDate hasta = LocalDate.now();
        LocalDate desde = switch (op) {
            case 2 -> hasta.minusDays(7);
            case 3 -> hasta.minusDays(30);
            default -> hasta;
        };
        String[] labels = {"", "HOY", "ÚLTIMA SEMANA", "ÚLTIMO MES"};
        List<String[]> registros = new ArrayList<>();
        double totalMonto = 0;
        for (Cliente c : banco.obtenerTodosClientes()) {
            for (Cuenta cu : banco.getCuentaRepo().obtenerPorPropietario(c.getId())) {
                for (Transaccion t : cu.getMovimientos()) {
                    LocalDate fechaTxn = t.getFecha().toLocalDate();
                    if (!fechaTxn.isBefore(desde) && !fechaTxn.isAfter(hasta)) {
                        registros.add(new String[]{
                                t.getId(), Formato.fecha(t.getFecha()),
                                c.getNombreCompleto(), cu.getNumeroCuenta(),
                                t.getTipo().getDescripcion(), Formato.moneda(t.getValor())
                        });
                        totalMonto += t.getValor();
                    }
                }
            }
        }
        Formato.titulo("REPORTE " + labels[op]);
        if (registros.isEmpty()) {
            System.out.println("  No hay transacciones en el período seleccionado.");
        } else {
            System.out.printf("  %-10s %-22s %-20s %-20s %-14s%n",
                    "ID", "Fecha", "Cliente", "Tipo", "Valor");
            Formato.separador('-', 90);
            for (String[] r : registros)
                System.out.printf("  %-10s %-22s %-20s %-20s %-14s%n",
                        r[0], r[1], r[2], r[4], r[5]);
            Formato.separador('-', 90);
            System.out.println("  Total transacciones: " + registros.size());
            System.out.println("  Monto total:         " + Formato.moneda(totalMonto));
        }
        Consola.pausar();
    }

    private void verSospechosas() {
        Formato.titulo("MIP-24 | TRANSACCIONES SOSPECHOSAS");
        double umbral = 5_000_000;
        List<String[]> sospechosas = new ArrayList<>();
        for (Cliente c : banco.obtenerTodosClientes()) {
            for (Cuenta cu : banco.getCuentaRepo().obtenerPorPropietario(c.getId())) {
                for (Transaccion t : cu.getMovimientos()) {
                    String nivel = "";
                    if (t.getValor() >= umbral)            nivel = "ALTO";
                    else if (t.getValor() >= umbral * 0.5) nivel = "MEDIO";
                    if (!nivel.isEmpty()) {
                        sospechosas.add(new String[]{
                                t.getId(), Formato.fecha(t.getFecha()),
                                c.getNombreCompleto(), t.getTipo().getDescripcion(),
                                Formato.moneda(t.getValor()), nivel
                        });
                    }
                }
            }
        }
        System.out.println("  Umbral de alerta: " + Formato.moneda(umbral));
        Formato.separador();
        if (sospechosas.isEmpty()) {
            System.out.println("  ✔  No hay transacciones sospechosas.");
        } else {
            System.out.printf("  %-10s %-22s %-20s %-18s %-14s %-8s%n",
                    "ID", "Fecha", "Cliente", "Tipo", "Monto", "Riesgo");
            Formato.separador('-', 95);
            for (String[] s : sospechosas)
                System.out.printf("  %-10s %-22s %-20s %-18s %-14s %-8s%n",
                        s[0], s[1], s[2], s[3], s[4], s[5]);
            Formato.separador('-', 95);
            System.out.println("  Total alertas: " + sospechosas.size());
        }
        Consola.pausar();
    }

    private void verUptime() {
        Formato.titulo("MIP-25 | UPTIME E INCIDENTES DEL CAJERO");
        int totalHoras     = 24 * 30;
        int horasIncidente = 2;
        double uptime      = ((double)(totalHoras - horasIncidente) / totalHoras) * 100;
        System.out.println("  Período:              Último mes (30 días)");
        System.out.printf("  Uptime:               %.2f%%%n", uptime);
        System.out.println("  Tiempo operativo:     " + (totalHoras - horasIncidente) + " horas");
        System.out.println("  Tiempo inactivo:      " + horasIncidente + " horas");
        System.out.println("  Total interrupciones: " + INCIDENTES.length);
        Formato.separador();
        Syste+m.out.printf("  %-10s %-25s %-20s %-20s %s%n",
                "ID", "Tipo", "Inicio", "Fin", "Estado");
        Formato.separador('-', 85);
        for (String[] inc : INCIDENTES)
            System.out.printf("  %-10s %-25s %-20s %-20s %s%n",
                    inc[0], inc[1], inc[2], inc[3], inc[4]);
        Formato.separador('-', 85);
        System.out.println("  Generado: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        Consola.pausar();
    }
}