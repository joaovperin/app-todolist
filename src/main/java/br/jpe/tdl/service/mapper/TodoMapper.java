package br.jpe.tdl.service.mapper;

import br.jpe.tdl.domain.*;
import br.jpe.tdl.service.dto.TodoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Todo} and its DTO {@link TodoDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TodoMapper extends EntityMapper<TodoDTO, Todo> {}
