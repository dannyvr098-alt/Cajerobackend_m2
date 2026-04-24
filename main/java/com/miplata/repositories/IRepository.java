package com.miplata.repositories;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico para todos los repositorios.
 * Define las operaciones CRUD completas sobre la lista en memoria.
 *
 * @param <T>  Tipo de entidad
 * @param <ID> Tipo del identificador
 */
public interface IRepository<T, ID> {

    /** Para agregar una nueva entidad a la lista. */
    T agregar(T entidad);

    /** Para retornar la entidad con el id dado, o vacío si no existe. */
    Optional<T> obtenerPorId(ID id);

    /** Para retornar una copia de toda la lista. */
    List<T> obtenerTodos();

    /** Para reemplazar la entidad existente con los nuevos datos. */
    T actualizar(T entidad);

    /** Para eliminar la entidad con el id dado. Lanza excepción si no existe. */
    boolean eliminar(ID id);

    /** Para hacer la cuenta total de entidades en la lista. */
    int contar();
}
