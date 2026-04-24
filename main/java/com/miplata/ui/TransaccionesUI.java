package com.miplata.ui;

import com.miplata.enums.RangoCuotas;
import com.miplata.enums.TipoCuenta;
import com.miplata.models.*;
import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

import java.util.List;

/**
 * Panel del cajero: todas las operaciones transaccionales.
 */
public class TransaccionesUI {

    private final BancoService banco;

    public TransaccionesUI(BancoService banco) {
        this.banco = banco;
    }

    // ════════════════════════════════════════════════════════════
    //  MENÚ PRINCIPAL DEL PANEL
    // ════════════════════════════════════════════════════════════
    public void mostrarPanel() {
        boolean activo = true;
        while (activo) {
            imprimirCabecera();
            mostrarMenuPanel();
            int op = Consola.leerOpcion("Elige una opción", 0, 8);
            System.out.println();
            switch (op) {
                case 1 -> consultarSaldo();
                case 2 -> consignar();
                case 3 -> retirar();
                case 4 -> verMovimientos();
                case 5 -> transferir();
                case 6 -> menuCredito();
                case 7 -> new PerfilUI(banco).mostrarPerfil();
                case 8 -> new AdminUI(banco).mostrarAdmin();
                case 0 -> {
                    if (Consola.confirmar("¿Deseas cerrar sesión?")) {
                        banco.cerrarSesion();
                        System.out.println("\n  ✔  Sesión cerrada. ¡Hasta pronto!");
                        Consola.pausar();
                        activo = false;
                    }
                }
            }
        }
    }

    private void imprimirCabecera() {
        Formato.titulo("MI PLATA — PANEL DEL CAJERO");
        Cliente c = banco.getClienteActual();
        System.out.printf("  👤 %s  |  @%s%n", c.getNombreCompleto(), c.getUsername());

        // Selector de cuenta activa
        List<Cuenta> cuentas = banco.getCuentasCliente();
        if (!cuentas.isEmpty()) {
            Cuenta activa = banco.getCuentaActiva();
            System.out.println("\n  Cuentas disponibles:");
            for (int i = 0; i < cuentas.size(); i++) {
                Cuenta cu = cuentas.get(i);
                String marcador = cu.getId().equals(activa != null ? activa.getId() : "") ? " ◀ activa" : "";
                System.out.printf("    [%d] %s — N° %s%s%n",
                        i + 1, cu.getTipo().getDescripcion(), cu.getNumeroCuenta(), marcador);
            }
            System.out.print("  » Cuenta activa [ENTER para mantener / número para cambiar]: ");
            String sel = new java.util.Scanner(System.in).nextLine().trim();
            if (!sel.isEmpty()) {
                try {
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx >= 0 && idx < cuentas.size())
                        banco.seleccionarCuenta(cuentas.get(idx).getId());
                } catch (Exception ignored) {}
            }
        }
        Formato.separador();
    }

    private void mostrarMenuPanel() {
        System.out.println("  [1] Consultar Saldo");
        System.out.println("  [2] Consignar Dinero");
        System.out.println("  [3] Retirar Dinero");
        System.out.println("  [4] Ver Movimientos");
        System.out.println("  [5] Transferir");
        System.out.println("  [6] Tarjeta de Crédito");
        System.out.println("  [7] Mi Perfil");
        System.out.println("  [8] Administrar Usuarios");
        System.out.println("  [0] Cerrar Sesión");
        Formato.separador();
    }

    // ════════════════════════════════════════════════════════════
    //  CONSULTAR SALDO
    // ════════════════════════════════════════════════════════════
    private void consultarSaldo() {
        Formato.titulo("CONSULTAR SALDO");
        Cuenta c = banco.getCuentaActiva();
        if (c == null) { sinCuenta(); return; }

        System.out.println("  Cuenta:  " + c.getTipo().getDescripcion());
        System.out.println("  Número:  " + c.getNumeroCuenta());
        System.out.println("  Estado:  " + c.getEstado().getDescripcion());

        if (c instanceof TarjetaCredito tc) {
            System.out.println("  Cupo total:       " + Formato.moneda(tc.getCupo()));
            System.out.println("  Deuda actual:     " + Formato.moneda(tc.getDeuda()));
            System.out.println("  Cupo disponible:  " + Formato.moneda(tc.getCupoDisponible()));
        } else {
            System.out.println("  Saldo actual:     " + Formato.moneda(c.getSaldo()));
            System.out.println("  Límite retiro:    " + Formato.moneda(c.getLimiteRetiro()));
            if (c instanceof CuentaAhorros)
                System.out.println("  Tasa interés:     1.5% mensual");
            if (c instanceof CuentaCorriente)
                System.out.println("  Sobregiro:        20% permitido");
        }
        Consola.pausar();
    }

    // ════════════════════════════════════════════════════════════
    //  CONSIGNAR
    // ════════════════════════════════════════════════════════════
    private void consignar() {
        Formato.titulo("CONSIGNAR DINERO");
        Cuenta c = banco.getCuentaActiva();
        if (c == null) { sinCuenta(); return; }

        System.out.println("  Cuenta: " + c.getTipo() + " — " + c.getNumeroCuenta());
        System.out.println("  Saldo actual: " + Formato.moneda(c.getSaldo()));
        System.out.println();

        double monto = Consola.leerMonto("Monto a consignar");
        try {
            double nuevo = banco.consignar(monto);
            System.out.println("\n  ✔  Consignación exitosa.");
            System.out.println("  Valor consignado: " + Formato.moneda(monto));
            System.out.println("  Nuevo saldo:      " + Formato.moneda(nuevo));
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    // ════════════════════════════════════════════════════════════
    //  RETIRAR
    // ════════════════════════════════════════════════════════════
    private void retirar() {
        Formato.titulo("RETIRAR DINERO");
        Cuenta c = banco.getCuentaActiva();
        if (c == null) { sinCuenta(); return; }

        if (c instanceof TarjetaCredito) {
            System.out.println("  ✗  Las tarjetas de crédito no permiten retiros en efectivo.");
            System.out.println("     Use la opción [6] Tarjeta de Crédito para compras.");
            Consola.pausar(); return;
        }

        System.out.println("  Cuenta:        " + c.getTipo() + " — " + c.getNumeroCuenta());
        System.out.println("  Saldo actual:  " + Formato.moneda(c.getSaldo()));
        System.out.println("  Límite retiro: " + Formato.moneda(c.getLimiteRetiro()));
        if (c instanceof CuentaAhorros)
            System.out.println("  ℹ  Se aplicará interés del 1.5% mensual al momento del retiro.");
        if (c instanceof CuentaCorriente)
            System.out.println("  ℹ  Sobregiro del 20% permitido.");
        System.out.println();

        double monto = Consola.leerMonto("Monto a retirar");
        try {
            double nuevo = banco.retirar(monto);
            System.out.println("\n  ✔  Retiro exitoso.");
            System.out.println("  Valor retirado: " + Formato.moneda(monto));
            System.out.println("  Nuevo saldo:    " + Formato.moneda(nuevo));
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    // ════════════════════════════════════════════════════════════
    //  VER MOVIMIENTOS
    // ════════════════════════════════════════════════════════════
    private void verMovimientos() {
        Formato.titulo("HISTORIAL DE MOVIMIENTOS");
        Cuenta c = banco.getCuentaActiva();
        if (c == null) { sinCuenta(); return; }

        System.out.println("  Cuenta: " + c.getTipo() + " — " + c.getNumeroCuenta());
        Formato.separador();

        List<Transaccion> movs = c.getMovimientos();
        if (movs.isEmpty()) {
            System.out.println("  Sin movimientos registrados aún.");
        } else {
            System.out.printf("  %-10s %-22s %-12s %s%n",
                    "ID", "Fecha", "Tipo", "Valor / Saldo");
            Formato.separador('-', 60);
            for (Transaccion t : movs) {
                System.out.printf("  %-10s %-22s %-20s %s → %s%n",
                        t.getId(),
                        Formato.fecha(t.getFecha()),
                        t.getTipo().getDescripcion(),
                        Formato.moneda(t.getValor()),
                        Formato.moneda(t.getSaldoResultante()));
                System.out.printf("             %s%n", t.getDescripcion());
                Formato.separador('-', 60);
            }
            System.out.println("  Total movimientos: " + movs.size());
        }
        Consola.pausar();
    }

    // ════════════════════════════════════════════════════════════
    //  TRANSFERIR
    // ════════════════════════════════════════════════════════════
    private void transferir() {
        Formato.titulo("TRANSFERIR DINERO");
        Cuenta c = banco.getCuentaActiva();
        if (c == null) { sinCuenta(); return; }

        System.out.println("  Cuenta origen: " + c.getTipo() + " — " + c.getNumeroCuenta());
        System.out.println("  Saldo actual:  " + Formato.moneda(c.getSaldo()));
        System.out.println();
        System.out.println("  Tipo de transferencia:");
        System.out.println("  [1] Entre mis propias cuentas");
        System.out.println("  [2] A cuenta de otro usuario");
        int tipo = Consola.leerOpcion("Opción", 1, 2);

        String destRef;
        if (tipo == 1) {
            List<Cuenta> mias = banco.getCuentasCliente();
            mias.removeIf(cu -> cu.getId().equals(c.getId()));
            if (mias.isEmpty()) {
                System.out.println("  ✗  No tienes otras cuentas disponibles como destino.");
                Consola.pausar(); return;
            }
            System.out.println("\n  Cuentas disponibles como destino:");
            for (int i = 0; i < mias.size(); i++)
                System.out.printf("    [%d] %s — %s%n", i+1,
                        mias.get(i).getTipo().getDescripcion(), mias.get(i).getNumeroCuenta());
            int idx = Consola.leerOpcion("Selecciona destino", 1, mias.size()) - 1;
            destRef = mias.get(idx).getId();
        } else {
            destRef = Consola.leerTexto("Número de cuenta destino (formato: XXXX-XXXX-XXXX-XXXX)");
        }

        double monto = Consola.leerMonto("Monto a transferir");
        try {
            banco.transferir(c.getId(), destRef, monto);
            System.out.println("\n  ✔  Transferencia exitosa.");
            System.out.println("  Monto enviado:  " + Formato.moneda(monto));
            System.out.println("  Nuevo saldo:    " + Formato.moneda(c.getSaldo()));
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    // ════════════════════════════════════════════════════════════
    //  TARJETA DE CRÉDITO
    // ════════════════════════════════════════════════════════════
    private void menuCredito() {
        Cuenta c = banco.getCuentaActiva();
        if (!(c instanceof TarjetaCredito)) {
            System.out.println("  ✗  La cuenta activa no es una Tarjeta de Crédito.");
            System.out.println("     Cámbiala desde el selector de cuentas.");
            Consola.pausar(); return;
        }
        TarjetaCredito tc = (TarjetaCredito) c;

        boolean activo = true;
        while (activo) {
            Formato.titulo("TARJETA DE CRÉDITO");
            System.out.println("  N° Tarjeta:      " + tc.getNumeroCuenta());
            System.out.println("  Cupo total:      " + Formato.moneda(tc.getCupo()));
            System.out.println("  Deuda actual:    " + Formato.moneda(tc.getDeuda()));
            System.out.println("  Cupo disponible: " + Formato.moneda(tc.getCupoDisponible()));
            Formato.separador();
            System.out.println("  [1] Realizar Compra");
            System.out.println("  [2] Pagar Deuda");
            System.out.println("  [3] Ver tabla de tasas");
            System.out.println("  [0] Volver");
            Formato.separador();

            switch (Consola.leerOpcion("Opción", 0, 3)) {
                case 1 -> realizarCompra(tc);
                case 2 -> pagarDeuda(tc);
                case 3 -> mostrarTasas();
                case 0 -> activo = false;
            }
        }
    }

    private void realizarCompra(TarjetaCredito tc) {
        Formato.titulo("REALIZAR COMPRA");
        System.out.println("  Cupo disponible: " + Formato.moneda(tc.getCupoDisponible()));
        System.out.println();
        mostrarTasas();

        double monto  = Consola.leerMonto("Monto de la compra");
        int    cuotas = Consola.leerEntero("Número de cuotas");

        // Preview antes de confirmar
        try {
            RangoCuotas rango    = RangoCuotas.obtenerPorCuotas(cuotas);
            double      tasa     = rango.getTasaMensual();
            double      cuotaMen = TarjetaCredito.calcularCuotaMensual(monto, tasa, cuotas);
            System.out.println("\n  ── Vista previa de la compra ──────────────────");
            System.out.println("  Monto:         " + Formato.moneda(monto));
            System.out.println("  Cuotas:        " + cuotas);
            System.out.println("  Tasa mensual:  " + String.format("%.1f%%", tasa * 100));
            System.out.println("  Cuota mensual: " + Formato.moneda(cuotaMen));
            System.out.println("  Total a pagar: " + Formato.moneda(cuotaMen * cuotas));
            Formato.separador();

            if (Consola.confirmar("¿Confirmar compra?")) {
                TarjetaCredito.ResultadoCompra res = banco.realizarCompra(monto, cuotas);
                System.out.println("\n  ✔  Compra realizada exitosamente.");
                System.out.println("  " + res);
            }
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    private void pagarDeuda(TarjetaCredito tc) {
        Formato.titulo("PAGAR DEUDA");
        System.out.println("  Deuda actual: " + Formato.moneda(tc.getDeuda()));
        if (tc.getDeuda() <= 0) {
            System.out.println("  ✔  No tienes deuda pendiente.");
            Consola.pausar(); return;
        }

        double monto = Consola.leerMonto("Monto a pagar (máx: " + Formato.moneda(tc.getDeuda()) + ")");
        try {
            double restante = banco.pagarTarjeta(monto);
            System.out.println("\n  ✔  Pago aplicado exitosamente.");
            System.out.println("  Pagado:          " + Formato.moneda(monto));
            System.out.println("  Deuda restante:  " + Formato.moneda(restante));
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    private void mostrarTasas() {
        System.out.println("  ┌─────────────────────────────────────────────┐");
        System.out.println("  │         Tabla de tasas de interés            │");
        System.out.println("  ├──────────────┬──────────────┬────────────────┤");
        System.out.println("  │   Cuotas     │ Tasa mensual │  Observación   │");
        System.out.println("  ├──────────────┼──────────────┼────────────────┤");
        System.out.println("  │  1 – 2       │     0.0%     │  Sin interés   │");
        System.out.println("  │  3 – 6       │     1.9%     │  Moderado      │");
        System.out.println("  │  7 o más     │     2.3%     │  Alto          │");
        System.out.println("  └──────────────┴──────────────┴────────────────┘");
        System.out.println();
    }

    // ── Helper ────────────────────────────────────────────────
    private void sinCuenta() {
        System.out.println("  ✗  No hay ninguna cuenta activa seleccionada.");
        Consola.pausar();
    }
}
