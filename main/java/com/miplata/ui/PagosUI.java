package com.miplata.ui;

import com.miplata.models.Cuenta;
import com.miplata.models.TarjetaCredito;
import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MIP-15 — Pagar servicios públicos y facturas desde el cajero.
 * MIP-16 — Recibir un comprobante impreso o digital de cada operación.
 */
public class PagosUI {

    private final BancoService banco;

    private static final String[][] SERVICIOS = {
            {"1", "Energía Eléctrica",    "EPM"},
            {"2", "Agua y Alcantarillado","ACUEDUCTO"},
            {"3", "Gas Natural",          "GASNATURALCOL"},
            {"4", "Internet",             "CLARO"},
            {"5", "Telefonía Móvil",      "MOVISTAR"},
            {"6", "Seguro de Vida",       "SURA"},
            {"7", "Televisión por Cable", "TIGO"},
            {"8", "Otro servicio",        "GENERICO"},
    };

    public PagosUI(BancoService banco) {
        this.banco = banco;
    }

    // MIP-15
    public void pagarServicio() {
        Formato.titulo("MIP-15 | PAGAR SERVICIOS Y FACTURAS");
        if (!banco.estaAutenticado()) {
            System.out.println("  ✗  Debe iniciar sesión primero.");
            Consola.pausar(); return;
        }
        Cuenta cuenta = banco.getCuentaActiva();
        if (cuenta == null) {
            System.out.println("  ✗  No hay ninguna cuenta activa.");
            Consola.pausar(); return;
        }
        if (cuenta instanceof TarjetaCredito) {
            System.out.println("  ✗  No se puede pagar con Tarjeta de Crédito.");
            Consola.pausar(); return;
        }
        System.out.println("  Servicios disponibles:");
        Formato.separador('-', 50);
        for (String[] s : SERVICIOS)
            System.out.printf("  [%s] %-25s %s%n", s[0], s[1], s[2]);
        Formato.separador('-', 50);
        int op = Consola.leerOpcion("Selecciona servicio", 1, SERVICIOS.length);
        String[] servicio = SERVICIOS[op - 1];
        String referencia = Consola.leerTexto("Número de referencia / factura");
        if (referencia.isBlank()) {
            System.out.println("  ✗  La referencia es obligatoria.");
            Consola.pausar(); return;
        }
        System.out.println("  Saldo disponible: " + Formato.moneda(cuenta.getSaldo()));
        double monto = Consola.leerMonto("Monto a pagar");
        if (monto > cuenta.getSaldo()) {
            System.out.println("  ✗  Saldo insuficiente.");
            Consola.pausar(); return;
        }
        Formato.separador();
        System.out.println("  Resumen del pago:");
        System.out.println("  ├─ Servicio:   " + servicio[1] + " (" + servicio[2] + ")");
        System.out.println("  ├─ Referencia: " + referencia);
        System.out.println("  ├─ Cuenta:     " + cuenta.getNumeroCuenta());
        System.out.println("  └─ Monto:      " + Formato.moneda(monto));
        Formato.separador();
        if (!Consola.confirmar("¿Confirmar pago?")) {
            System.out.println("  Pago cancelado.");
            Consola.pausar(); return;
        }
        try {
            banco.retirar(monto);
            String ref = "PAG-" + System.currentTimeMillis();
            System.out.println("\n  ✔  Pago realizado exitosamente.");
            System.out.println("  Referencia: " + ref);
            generarComprobante("PAGO DE SERVICIO", servicio[1], referencia, monto,
                    cuenta.getNumeroCuenta(), ref);
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
            Consola.pausar();
        }
    }

    // MIP-16
    public void generarComprobante(String tipoOperacion, String detalle,
                                   String referencia, double monto,
                                   String numeroCuenta, String refTransaccion) {
        Formato.separador();
        System.out.println("  ¿Desea recibir comprobante?");
        System.out.println("  [1] Sí, mostrar en pantalla");
        System.out.println("  [2] Sí, enviar digital (SMS/correo)");
        System.out.println("  [3] No");
        Formato.separador();
        switch (Consola.leerOpcion("Opción", 1, 3)) {
            case 1 -> imprimirComprobante(tipoOperacion, detalle, referencia,
                    monto, numeroCuenta, refTransaccion);
            case 2 -> enviarDigital(tipoOperacion, monto, refTransaccion);
            case 3 -> System.out.println("  Comprobante omitido.");
        }
        Consola.pausar();
    }

    private void imprimirComprobante(String tipoOperacion, String detalle,
                                     String referencia, double monto,
                                     String numeroCuenta, String refTransaccion) {
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║         MI PLATA — COMPROBANTE           ║");
        System.out.println("  ╠══════════════════════════════════════════╣");
        System.out.printf("  ║  Fecha:      %-29s║%n", fecha);
        System.out.printf("  ║  Operación:  %-29s║%n", tipoOperacion);
        System.out.printf("  ║  Detalle:    %-29s║%n", detalle);
        System.out.printf("  ║  Referencia: %-29s║%n", referencia);
        System.out.printf("  ║  Cuenta:     %-29s║%n", numeroCuenta);
        System.out.printf("  ║  Monto:      %-29s║%n", Formato.moneda(monto));
        System.out.printf("  ║  Ref. Trans: %-29s║%n", refTransaccion);
        System.out.println("  ╠══════════════════════════════════════════╣");
        System.out.println("  ║  Estado: APROBADO ✔                      ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
    }

    private void enviarDigital(String tipoOperacion, double monto, String refTransaccion) {
        String cliente = banco.getClienteActual().getNombreCompleto();
        String celular = banco.getClienteActual().getCelular();
        System.out.println("\n  📱 Comprobante digital enviado:");
        System.out.println("  Para:    " + cliente);
        System.out.println("  Cel:     " + celular.substring(0, 3) + "*****" + celular.substring(8));
        System.out.println("  MiPlata: " + tipoOperacion + " — " + Formato.moneda(monto));
        System.out.println("  Ref:     " + refTransaccion);
        System.out.println("  ✔  Notificación enviada.");
    }
}