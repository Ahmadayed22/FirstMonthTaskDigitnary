package com.todolist.repo;

import java.util.List;
import java.util.Optional;
public interface Repository<T, DT> {
    T save(T entity);

    Optional<T> findById(DT id);

    List<T> findAll();

    void deleteById(DT id);

    boolean existsById(DT id);
}
