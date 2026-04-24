package com.miplata.models;

import com.miplata.enums.EstadoUsuario;


public class Cliente {

    private static final int MAX_INTENTOS = 3;

    // ── Atributos privados ────────────────────────────────────
    private final String        id;
    private       String        identificacion;
    private       String        nombreCompleto;
    private       String        celular;
    private final String        username;
    private       String        passwordHash;
    private       EstadoUsuario estado;
    private       int           intentosFallidos;

    // ── Constructor con validaciones completas ────────────────
    public Cliente(String identificacion, String nombreCompleto,
                   String celular, String username, String password) {
        validarIdentificacion(identificacion);
        validarNombre(nombreCompleto);
        validarCelular(celular);
        validarUsername(username);
        validarPassword(password);

        this.id               = "USR-" + System.currentTimeMillis();
        this.identificacion   = identificacion.trim();
        this.nombreCompleto   = nombreCompleto.trim();
        this.celular          = celular.trim();
        this.username         = username.trim().toLowerCase();
        this.passwordHash     = hashPassword(password);
        this.estado           = EstadoUsuario.ACTIVO;
        this.intentosFallidos = 0;
    }


    //  AUTENTICACIÓN CLIENTE


    public boolean verificarPassword(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    /** Aquí se registra un intento fallido. Retorna los intentos acumulados. */
    public int registrarIntentoFallido() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= MAX_INTENTOS)
            this.estado = EstadoUsuario.BLOQUEADO;
        return this.intentosFallidos;
    }

    public void resetearIntentos() { this.intentosFallidos = 0; }

    public void desbloquear() {
        this.estado = EstadoUsuario.ACTIVO;
        this.intentosFallidos = 0;
    }

    public boolean estaBloqueado() { return this.estado == EstadoUsuario.BLOQUEADO; }

    //  EDICIÓN DE PERFIL

    public void actualizarPerfil(String identificacion, String nombreCompleto, String celular) {
        if (identificacion != null && !identificacion.isBlank()) {
            validarIdentificacion(identificacion);
            this.identificacion = identificacion.trim();
        }
        if (nombreCompleto != null && !nombreCompleto.isBlank()) {
            validarNombre(nombreCompleto);
            this.nombreCompleto = nombreCompleto.trim();
        }
        if (celular != null && !celular.isBlank()) {
            validarCelular(celular);
            this.celular = celular.trim();
        }
    }

    public void cambiarPassword(String actual, String nueva, String confirmacion) throws Exception {
        if (!verificarPassword(actual))
            throw new Exception("La contraseña actual es incorrecta.");
        validarPassword(nueva);
        if (!nueva.equals(confirmacion))
            throw new Exception("La nueva contraseña y su confirmación no coinciden.");
        this.passwordHash = hashPassword(nueva);
    }

    //  VALIDACIONES ESTÁTICAS


    public static void validarIdentificacion(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("La identificación es obligatoria.");
        if (!id.trim().matches("\\d{6,12}"))
            throw new IllegalArgumentException("La identificación debe tener entre 6 y 12 dígitos numéricos.");
    }

    public static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        if (nombre.trim().length() < 3)
            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres.");
    }

    public static void validarCelular(String celular) {
        if (celular == null || celular.isBlank())
            throw new IllegalArgumentException("El celular es obligatorio.");
        if (!celular.trim().matches("\\d{10}"))
            throw new IllegalArgumentException("El celular debe tener exactamente 10 dígitos numéricos.");
    }

    public static void validarUsername(String username) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (username.trim().length() < 4)
            throw new IllegalArgumentException("El usuario debe tener al menos 4 caracteres.");
        if (!username.trim().matches("[a-zA-Z0-9_]+"))
            throw new IllegalArgumentException("El usuario solo puede contener letras, números y guión bajo (_).");
    }

    public static void validarPassword(String password) {
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        if (!password.matches(".*[A-Z].*"))
            throw new IllegalArgumentException("La contraseña debe contener al menos una letra mayúscula.");
        if (!password.matches(".*\\d.*"))
            throw new IllegalArgumentException("La contraseña debe contener al menos un número.");
    }

    private static String hashPassword(String password) {
        int hash = 0;
        for (char c : password.toCharArray()) {
            hash = (hash * 31 + c) & 0x7fffffff;
        }
        return "H" + Integer.toHexString(hash).toUpperCase();
    }

    //  GETTERS PÚBLICOS

    public String        getId()               { return id; }
    public String        getIdentificacion()   { return identificacion; }
    public String        getNombreCompleto()   { return nombreCompleto; }
    public String        getCelular()          { return celular; }
    public String        getUsername()         { return username; }
    public EstadoUsuario getEstado()           { return estado; }
    public int           getIntentosFallidos() { return intentosFallidos; }

    @Override
    public String toString() {
        return String.format("Cliente[%s | @%s | %s | Estado: %s]",
                nombreCompleto, username, identificacion, estado);
    }
}
