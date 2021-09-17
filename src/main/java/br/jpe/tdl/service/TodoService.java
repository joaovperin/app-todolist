package br.jpe.tdl.service;

import br.jpe.tdl.service.dto.TodoDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link br.jpe.tdl.domain.Todo}.
 */
public interface TodoService {
    /**
     * Save a todo.
     *
     * @param todoDTO the entity to save.
     * @return the persisted entity.
     */
    TodoDTO save(TodoDTO todoDTO);

    /**
     * Partially updates a todo.
     *
     * @param todoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TodoDTO> partialUpdate(TodoDTO todoDTO);

    /**
     * Get all the todos.
     *
     * @return the list of entities.
     */
    List<TodoDTO> findAll();

    /**
     * Get the "id" todo.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TodoDTO> findOne(Long id);

    /**
     * Delete the "id" todo.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
