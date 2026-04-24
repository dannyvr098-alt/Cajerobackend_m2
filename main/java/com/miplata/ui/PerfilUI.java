package com.miplata.ui;

import com.miplata.models.Cliente;
import com.miplata.services.BancoService;
import com.miplata.utils.Consola;
import com.miplata.utils.Formato;

/**
 * Vista de Perfil y Seguridad del usuario autenticado.
 */
public class PerfilUI {

    private final BancoService banco;

    public PerfilUI(BancoService banco) {
        this.banco = banco;
    }

    public void mostrarPerfil() {
        boolean activo = true;
        while (activo) {
            Cliente c = banco.getClienteActual();
            Formato.titulo("MI PERFIL");
            System.out.println("  Identificación: " + c.getIdentificacion());
            System.out.println("  Nombre:         " + c.getNombreCompleto());
            System.out.println("  Celular:        " + c.getCelular());
            System.out.println("  Usuario:        @" + c.getUsername());
            System.out.println("  Estado:         " + c.getEstado().getDescripcion());
            Formato.separador();
            System.out.println("  [1] Editar datos personales");
            System.out.println("  [2] Cambiar contraseña");
            System.out.println("  [0] Volver");
            Formato.separador();

            switch (Consola.leerOpcion("Opción", 0, 2)) {
                case 1 -> editarDatos();
                case 2 -> cambiarPassword();
                case 0 -> activo = false;
            }
        }
    }

    // ── Editar datos personales ───────────────────────────────
    private void editarDatos() {
        Formato.titulo("EDITAR DATOS PERSONALES");
        Cliente c = banco.getClienteActual();
        System.out.println("  (Deja en blanco para mantener el valor actual)\n");

        String id     = Consola.leerTexto("Identificación [" + c.getIdentificacion() + "]");
        String nombre = Consola.leerTexto("Nombre completo [" + c.getNombreCompleto() + "]");
        String cel    = Consola.leerTexto("Celular         [" + c.getCelular() + "]");

        try {
            banco.actualizarPerfil(
                    id.isEmpty()     ? null : id,
                    nombre.isEmpty() ? null : nombre,
                    cel.isEmpty()    ? null : cel);
            System.out.println("\n  ✔  Perfil actualizado correctamente.");
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }

    // ── Cambiar contraseña ────────────────────────────────────
    private void cambiarPassword() {
        Formato.titulo("CAMBIAR CONTRASEÑA");
        System.out.println("  Pasos: contraseña actual → nueva → confirmar\n");

        String actual      = Consola.leerPassword("Contraseña actual");
        String nueva       = Consola.leerPassword("Nueva contraseña (mín. 6 chars, 1 mayúscula, 1 número)");
        String confirmacion= Consola.leerPassword("Confirmar nueva contraseña");

        try {
            banco.cambiarPassword(actual, nueva, confirmacion);
            System.out.println("\n  ✔  Contraseña cambiada exitosamente.");
        } catch (Exception e) {
            System.out.println("  ✗  " + e.getMessage());
        }
        Consola.pausar();
    }
}
