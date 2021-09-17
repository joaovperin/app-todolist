package br.jpe.tdl.service.impl;

import br.jpe.tdl.domain.Todo;
import br.jpe.tdl.repository.TodoRepository;
import br.jpe.tdl.service.TodoService;
import br.jpe.tdl.service.dto.TodoDTO;
import br.jpe.tdl.service.mapper.TodoMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Todo}.
 */
@Service
@Transactional
public class TodoServiceImpl implements TodoService {

    private final Logger log = LoggerFactory.getLogger(TodoServiceImpl.class);

    private final TodoRepository todoRepository;

    private final TodoMapper todoMapper;

    public TodoServiceImpl(TodoRepository todoRepository, TodoMapper todoMapper) {
        this.todoRepository = todoRepository;
        this.todoMapper = todoMapper;
    }

    @Override
    public TodoDTO save(TodoDTO todoDTO) {
        log.debug("Request to save Todo : {}", todoDTO);
        Todo todo = todoMapper.toEntity(todoDTO);
        todo = todoRepository.save(todo);
        return todoMapper.toDto(todo);
    }

    @Override
    public Optional<TodoDTO> partialUpdate(TodoDTO todoDTO) {
        log.debug("Request to partially update Todo : {}", todoDTO);

        return todoRepository
            .findById(todoDTO.getId())
            .map(existingTodo -> {
                todoMapper.partialUpdate(existingTodo, todoDTO);

                return existingTodo;
            })
            .map(todoRepository::save)
            .map(todoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoDTO> findAll() {
        log.debug("Request to get all Todos");
        return todoRepository.findAll().stream().map(todoMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TodoDTO> findOne(Long id) {
        log.debug("Request to get Todo : {}", id);
        return todoRepository.findById(id).map(todoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Todo : {}", id);
        todoRepository.deleteById(id);
    }
}
