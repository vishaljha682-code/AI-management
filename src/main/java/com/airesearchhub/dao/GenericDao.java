package com.airesearchhub.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO Interface demonstrating Java Generics & Collections.
 * Part of the Data Access Layer meeting project rubric standards.
 *
 * @param <T>  The entity type
 * @param <ID> The entity primary key type
 */
public interface GenericDao<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    boolean update(T entity);
    boolean deleteById(ID id);
    long count();
}
