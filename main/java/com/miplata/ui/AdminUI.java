package com.miplata.ui;

import domain.TipoCuenta;
import com.miplata.models.Cliente;
import com.miplata.models.Cuenta;
import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

import java.util.List;
// feat(MIP-2): Registrar nuevos clientes y asignarles una cuenta
public class AdminUI {

    private final BancoService banco;

    public AdminUI(BancoService banco) {
        this.banco = banco;
    }

    public void mostrarAdmin() {
        boolean activo = true;
        while (activo) {
            Formato.titulo("ADMINISTRACIÓN DE USUARIOS");
            System.out.println("  [1] Listar todos los clientes");
            System.out.println("  [2] Agregar nuevo cliente");
            System.out.println("  [3] Editar cliente");
            System.out.println("  [4] Eliminar cliente");
            System.out.println("  [5] Desbloquear cliente");
            System.out.println("  [6] Ver cuentas de un cliente");
            System.out.println("  [7] Agregar cuenta a un cliente");
            System.out.println("  [0] Volver");
            Formato.separador();

            switch (Consola.leerOpcion("Opción", 0, 7)) {
                case 1 -> listarClientes();
                case 2 -> agregarCliente();
                case 3 -> editarCliente();
                case 4 -> eliminarCliente();
                case 5 -> desbloquearCliente();
                case 6 -> verCuentasCliente();
                case 7 -> agregarCuentaACliente();
                case 0 -> activo = false;
            }
        }
    }

    // ── 1. Listar ─────────────────────────────────────────────
    private void listarClientes() {
        Formato.titulo("LISTA DE CLIENTES");
        List<Cliente> todos = banco.obtenerTodosClientes();

        if (todos.isEmpty()) {
            System.out.println("  No hay clientes registrados.");
        } else {
            System.out.printf("  %-6s %-14s %-22s %-12s %-10s%n",
                    "#", "Identificación", "Nombre", "Usuario", "Estado");
            Formato.separador('-', 70);
            int i = 1;
            for (Cliente c : todos) {
                System.out.printf("  %-6d %-14s %-22s %-12s %-10s%n",
                        i++, c.getIdentificacion(), c.getNombreCompleto(),
                        "@" + c.getUsername(), c.getEstado().getDescripcion());
            }
            System.out.println("\n  Total: " + todos.size() + " cliente(s).");
        }
        Consola.pausar();
    }

    // ── 2. Agregar ────────────────────────────────────────────
    private void agregarCliente() {
        Formato.titulo("AGREGAR NUEVO CLIENTE");
        try {
            String id       = Consola.leerTexto("Identificación");
            String nombre   = Consola.leerTexto("Nombre completo");
            String celular  = Consola.leerTexto("Celular");
            String username = Consola.leerTexto("Nombre de usuario");
            String password = Consola.leerPassword("Contraseña");
            String confirm  = Consola.leerPassword("Confirmar contraseña");

            if (!password.equals(confirm)) {
                System.out.println("  ✗  Las contraseñas no coinciden.");
                Consola.pausar(); return;
            }

            System.out.println("\n  Tipo de cuenta inicial:");
            System.out.println("  [1] Ahorros  [2] Corriente  [3] Crédito");
            int opTipo = Consola.leerOpcion("Opción", 1, 3);
            TipoCuenta tipo = switch (opTipo) {
                case 2 -> TipoCuenta.CORRIENTE;
                case 3 -> TipoCuenta.CREDITO;
                default -> TipoCuenta.AHORROS;
            };
            double valor = Consola.leerMonto(tipo == TipoCuenta.CREDITO ? "Cupo de crédito" : "Saldo inicial");

            Cliente nuevo = banco.registrarCliente(id, nombre, celular, username, password, tipo, valor);
            System.out.println("\n  ✔  Cliente creado: " + nuevo.getNombreCompleto() + " (@" + nuevo.getUsername() + ")");
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    // ── 3. Editar ─────────────────────────────────────────────
    private void editarCliente() {
        Formato.titulo("EDITAR CLIENTE");
        Cliente c = seleccionarCliente();
        if (c == null) return;

        System.out.println("  (Deja en blanco para mantener el valor actual)\n");
        String id     = Consola.leerTexto("Identificación [" + c.getIdentificacion() + "]");
        String nombre = Consola.leerTexto("Nombre         [" + c.getNombreCompleto() + "]");
        String cel    = Consola.leerTexto("Celular        [" + c.getCelular() + "]");

        try {
            c.actualizarPerfil(
                    id.isEmpty()     ? null : id,
                    nombre.isEmpty() ? null : nombre,
                    cel.isEmpty()    ? null : cel);
            System.out.println("\n  ✔  Cliente actualizado correctamente.");
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    // ── 4. Eliminar ───────────────────────────────────────────
    private void eliminarCliente() {
        Formato.titulo("ELIMINAR CLIENTE");
        Cliente c = seleccionarCliente();
        if (c == null) return;

        System.out.println("  Cliente a eliminar: " + c.getNombreCompleto() + " (@" + c.getUsername() + ")");
        System.out.println("  ADVERTENCIA: Se eliminarán también todas sus cuentas.");

        if (Consola.confirmar("¿Confirmar eliminación?")) {
            try {
                banco.eliminarCliente(c.getId());
                System.out.println("  ✔  Cliente eliminado.");
            } catch (Exception e) {
                System.out.println("  ✗  " + e.getMessage());
            }
        }
        Consola.pausar();
    }

    // ── 5. Desbloquear ────────────────────────────────────────
    private void desbloquearCliente() {
        Formato.titulo("DESBLOQUEAR CLIENTE");
        List<Cliente> bloqueados = banco.getClienteRepo().obtenerBloqueados();
        if (bloqueados.isEmpty()) {
            System.out.println("  No hay clientes bloqueados.");
            Consola.pausar(); return;
        }

        System.out.println("  Clientes bloqueados:");
        for (int i = 0; i < bloqueados.size(); i++)
            System.out.printf("  [%d] %s (@%s)%n", i+1,
                    bloqueados.get(i).getNombreCompleto(), bloqueados.get(i).getUsername());

        int idx = Consola.leerOpcion("Selecciona cliente", 1, bloqueados.size()) - 1;
        try {
            banco.desbloquearCliente(bloqueados.get(idx).getId());
            System.out.println("  ✔  Cliente desbloqueado.");
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    // ── 6. Ver cuentas ────────────────────────────────────────
    private void verCuentasCliente() {
        Formato.titulo("CUENTAS DE UN CLIENTE");
        Cliente c = seleccionarCliente();
        if (c == null) return;

        List<Cuenta> cuentas = banco.getCuentaRepo().obtenerPorPropietario(c.getId());
        System.out.println("  Cliente: " + c.getNombreCompleto());
        Formato.separador('-', 60);
        if (cuentas.isEmpty()) {
            System.out.println("  Sin cuentas registradas.");
        } else {
            for (Cuenta cu : cuentas)
                System.out.println("  → " + cu);
        }
        Consola.pausar();
    }

    // ── 7. Agregar cuenta ─────────────────────────────────────
    private void agregarCuentaACliente() {
        Formato.titulo("AGREGAR CUENTA A CLIENTE");
        Cliente c = seleccionarCliente();
        if (c == null) return;

        System.out.println("  [1] Ahorros  [2] Corriente  [3] Crédito");
        int op = Consola.leerOpcion("Tipo de cuenta", 1, 3);
        TipoCuenta tipo = switch (op) {
            case 2 -> TipoCuenta.CORRIENTE;
            case 3 -> TipoCuenta.CREDITO;
            default -> TipoCuenta.AHORROS;
        };
        double valor = Consola.leerMonto(tipo == TipoCuenta.CREDITO ? "Cupo de crédito" : "Saldo inicial");

        try {
            banco.agregarCuentaACliente(c.getId(), tipo, valor);
            System.out.println("  ✔  Cuenta creada para " + c.getNombreCompleto() + ".");
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    // ── Helper: seleccionar cliente de la lista ───────────────
    private Cliente seleccionarCliente() {
        List<Cliente> todos = banco.obtenerTodosClientes();
        if (todos.isEmpty()) {
            System.out.println("  No hay clientes registrados.");
            Consola.pausar(); return null;
        }

        System.out.println("  Clientes registrados:");
        for (int i = 0; i < todos.size(); i++)
            System.out.printf("  [%d] %s (@%s)%n", i+1,
                    todos.get(i).getNombreCompleto(), todos.get(i).getUsername());

        int idx = Consola.leerOpcion("Selecciona cliente", 1, todos.size()) - 1;
        return todos.get(idx);
    }
}
