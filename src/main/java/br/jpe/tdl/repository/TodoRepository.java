package br.jpe.tdl.repository;

import br.jpe.tdl.domain.Todo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Todo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, JpaSpecificationExecutor<Todo> {}
