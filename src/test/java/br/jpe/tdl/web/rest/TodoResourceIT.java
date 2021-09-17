package br.jpe.tdl.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.jpe.tdl.IntegrationTest;
import br.jpe.tdl.domain.Todo;
import br.jpe.tdl.repository.TodoRepository;
import br.jpe.tdl.service.criteria.TodoCriteria;
import br.jpe.tdl.service.dto.TodoDTO;
import br.jpe.tdl.service.mapper.TodoMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TodoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TodoResourceIT {

    private static final String DEFAULT_SUBJECT = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT = "BBBBBBBBBB";

    private static final String DEFAULT_DETAIL = "AAAAAAAAAA";
    private static final String UPDATED_DETAIL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/todos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoMapper todoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTodoMockMvc;

    private Todo todo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Todo createEntity(EntityManager em) {
        Todo todo = new Todo().subject(DEFAULT_SUBJECT).detail(DEFAULT_DETAIL);
        return todo;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Todo createUpdatedEntity(EntityManager em) {
        Todo todo = new Todo().subject(UPDATED_SUBJECT).detail(UPDATED_DETAIL);
        return todo;
    }

    @BeforeEach
    public void initTest() {
        todo = createEntity(em);
    }

    @Test
    @Transactional
    void createTodo() throws Exception {
        int databaseSizeBeforeCreate = todoRepository.findAll().size();
        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);
        restTodoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(todoDTO)))
            .andExpect(status().isCreated());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeCreate + 1);
        Todo testTodo = todoList.get(todoList.size() - 1);
        assertThat(testTodo.getSubject()).isEqualTo(DEFAULT_SUBJECT);
        assertThat(testTodo.getDetail()).isEqualTo(DEFAULT_DETAIL);
    }

    @Test
    @Transactional
    void createTodoWithExistingId() throws Exception {
        // Create the Todo with an existing ID
        todo.setId(1L);
        TodoDTO todoDTO = todoMapper.toDto(todo);

        int databaseSizeBeforeCreate = todoRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTodoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(todoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSubjectIsRequired() throws Exception {
        int databaseSizeBeforeTest = todoRepository.findAll().size();
        // set the field null
        todo.setSubject(null);

        // Create the Todo, which fails.
        TodoDTO todoDTO = todoMapper.toDto(todo);

        restTodoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(todoDTO)))
            .andExpect(status().isBadRequest());

        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTodos() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList
        restTodoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(todo.getId().intValue())))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(DEFAULT_SUBJECT)))
            .andExpect(jsonPath("$.[*].detail").value(hasItem(DEFAULT_DETAIL)));
    }

    @Test
    @Transactional
    void getTodo() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get the todo
        restTodoMockMvc
            .perform(get(ENTITY_API_URL_ID, todo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(todo.getId().intValue()))
            .andExpect(jsonPath("$.subject").value(DEFAULT_SUBJECT))
            .andExpect(jsonPath("$.detail").value(DEFAULT_DETAIL));
    }

    @Test
    @Transactional
    void getTodosByIdFiltering() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        Long id = todo.getId();

        defaultTodoShouldBeFound("id.equals=" + id);
        defaultTodoShouldNotBeFound("id.notEquals=" + id);

        defaultTodoShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultTodoShouldNotBeFound("id.greaterThan=" + id);

        defaultTodoShouldBeFound("id.lessThanOrEqual=" + id);
        defaultTodoShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTodosBySubjectIsEqualToSomething() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where subject equals to DEFAULT_SUBJECT
        defaultTodoShouldBeFound("subject.equals=" + DEFAULT_SUBJECT);

        // Get all the todoList where subject equals to UPDATED_SUBJECT
        defaultTodoShouldNotBeFound("subject.equals=" + UPDATED_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTodosBySubjectIsNotEqualToSomething() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where subject not equals to DEFAULT_SUBJECT
        defaultTodoShouldNotBeFound("subject.notEquals=" + DEFAULT_SUBJECT);

        // Get all the todoList where subject not equals to UPDATED_SUBJECT
        defaultTodoShouldBeFound("subject.notEquals=" + UPDATED_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTodosBySubjectIsInShouldWork() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where subject in DEFAULT_SUBJECT or UPDATED_SUBJECT
        defaultTodoShouldBeFound("subject.in=" + DEFAULT_SUBJECT + "," + UPDATED_SUBJECT);

        // Get all the todoList where subject equals to UPDATED_SUBJECT
        defaultTodoShouldNotBeFound("subject.in=" + UPDATED_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTodosBySubjectIsNullOrNotNull() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where subject is not null
        defaultTodoShouldBeFound("subject.specified=true");

        // Get all the todoList where subject is null
        defaultTodoShouldNotBeFound("subject.specified=false");
    }

    @Test
    @Transactional
    void getAllTodosBySubjectContainsSomething() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where subject contains DEFAULT_SUBJECT
        defaultTodoShouldBeFound("subject.contains=" + DEFAULT_SUBJECT);

        // Get all the todoList where subject contains UPDATED_SUBJECT
        defaultTodoShouldNotBeFound("subject.contains=" + UPDATED_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTodosBySubjectNotContainsSomething() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where subject does not contain DEFAULT_SUBJECT
        defaultTodoShouldNotBeFound("subject.doesNotContain=" + DEFAULT_SUBJECT);

        // Get all the todoList where subject does not contain UPDATED_SUBJECT
        defaultTodoShouldBeFound("subject.doesNotContain=" + UPDATED_SUBJECT);
    }

    @Test
    @Transactional
    void getAllTodosByDetailIsEqualToSomething() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where detail equals to DEFAULT_DETAIL
        defaultTodoShouldBeFound("detail.equals=" + DEFAULT_DETAIL);

        // Get all the todoList where detail equals to UPDATED_DETAIL
        defaultTodoShouldNotBeFound("detail.equals=" + UPDATED_DETAIL);
    }

    @Test
    @Transactional
    void getAllTodosByDetailIsNotEqualToSomething() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where detail not equals to DEFAULT_DETAIL
        defaultTodoShouldNotBeFound("detail.notEquals=" + DEFAULT_DETAIL);

        // Get all the todoList where detail not equals to UPDATED_DETAIL
        defaultTodoShouldBeFound("detail.notEquals=" + UPDATED_DETAIL);
    }

    @Test
    @Transactional
    void getAllTodosByDetailIsInShouldWork() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where detail in DEFAULT_DETAIL or UPDATED_DETAIL
        defaultTodoShouldBeFound("detail.in=" + DEFAULT_DETAIL + "," + UPDATED_DETAIL);

        // Get all the todoList where detail equals to UPDATED_DETAIL
        defaultTodoShouldNotBeFound("detail.in=" + UPDATED_DETAIL);
    }

    @Test
    @Transactional
    void getAllTodosByDetailIsNullOrNotNull() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where detail is not null
        defaultTodoShouldBeFound("detail.specified=true");

        // Get all the todoList where detail is null
        defaultTodoShouldNotBeFound("detail.specified=false");
    }

    @Test
    @Transactional
    void getAllTodosByDetailContainsSomething() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where detail contains DEFAULT_DETAIL
        defaultTodoShouldBeFound("detail.contains=" + DEFAULT_DETAIL);

        // Get all the todoList where detail contains UPDATED_DETAIL
        defaultTodoShouldNotBeFound("detail.contains=" + UPDATED_DETAIL);
    }

    @Test
    @Transactional
    void getAllTodosByDetailNotContainsSomething() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        // Get all the todoList where detail does not contain DEFAULT_DETAIL
        defaultTodoShouldNotBeFound("detail.doesNotContain=" + DEFAULT_DETAIL);

        // Get all the todoList where detail does not contain UPDATED_DETAIL
        defaultTodoShouldBeFound("detail.doesNotContain=" + UPDATED_DETAIL);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTodoShouldBeFound(String filter) throws Exception {
        restTodoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(todo.getId().intValue())))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(DEFAULT_SUBJECT)))
            .andExpect(jsonPath("$.[*].detail").value(hasItem(DEFAULT_DETAIL)));

        // Check, that the count call also returns 1
        restTodoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTodoShouldNotBeFound(String filter) throws Exception {
        restTodoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTodoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTodo() throws Exception {
        // Get the todo
        restTodoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewTodo() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        int databaseSizeBeforeUpdate = todoRepository.findAll().size();

        // Update the todo
        Todo updatedTodo = todoRepository.findById(todo.getId()).get();
        // Disconnect from session so that the updates on updatedTodo are not directly saved in db
        em.detach(updatedTodo);
        updatedTodo.subject(UPDATED_SUBJECT).detail(UPDATED_DETAIL);
        TodoDTO todoDTO = todoMapper.toDto(updatedTodo);

        restTodoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, todoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(todoDTO))
            )
            .andExpect(status().isOk());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
        Todo testTodo = todoList.get(todoList.size() - 1);
        assertThat(testTodo.getSubject()).isEqualTo(UPDATED_SUBJECT);
        assertThat(testTodo.getDetail()).isEqualTo(UPDATED_DETAIL);
    }

    @Test
    @Transactional
    void putNonExistingTodo() throws Exception {
        int databaseSizeBeforeUpdate = todoRepository.findAll().size();
        todo.setId(count.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, todoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(todoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTodo() throws Exception {
        int databaseSizeBeforeUpdate = todoRepository.findAll().size();
        todo.setId(count.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(todoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTodo() throws Exception {
        int databaseSizeBeforeUpdate = todoRepository.findAll().size();
        todo.setId(count.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(todoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTodoWithPatch() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        int databaseSizeBeforeUpdate = todoRepository.findAll().size();

        // Update the todo using partial update
        Todo partialUpdatedTodo = new Todo();
        partialUpdatedTodo.setId(todo.getId());

        partialUpdatedTodo.subject(UPDATED_SUBJECT).detail(UPDATED_DETAIL);

        restTodoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTodo.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTodo))
            )
            .andExpect(status().isOk());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
        Todo testTodo = todoList.get(todoList.size() - 1);
        assertThat(testTodo.getSubject()).isEqualTo(UPDATED_SUBJECT);
        assertThat(testTodo.getDetail()).isEqualTo(UPDATED_DETAIL);
    }

    @Test
    @Transactional
    void fullUpdateTodoWithPatch() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        int databaseSizeBeforeUpdate = todoRepository.findAll().size();

        // Update the todo using partial update
        Todo partialUpdatedTodo = new Todo();
        partialUpdatedTodo.setId(todo.getId());

        partialUpdatedTodo.subject(UPDATED_SUBJECT).detail(UPDATED_DETAIL);

        restTodoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTodo.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTodo))
            )
            .andExpect(status().isOk());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
        Todo testTodo = todoList.get(todoList.size() - 1);
        assertThat(testTodo.getSubject()).isEqualTo(UPDATED_SUBJECT);
        assertThat(testTodo.getDetail()).isEqualTo(UPDATED_DETAIL);
    }

    @Test
    @Transactional
    void patchNonExistingTodo() throws Exception {
        int databaseSizeBeforeUpdate = todoRepository.findAll().size();
        todo.setId(count.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, todoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(todoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTodo() throws Exception {
        int databaseSizeBeforeUpdate = todoRepository.findAll().size();
        todo.setId(count.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(todoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTodo() throws Exception {
        int databaseSizeBeforeUpdate = todoRepository.findAll().size();
        todo.setId(count.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(todoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Todo in the database
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTodo() throws Exception {
        // Initialize the database
        todoRepository.saveAndFlush(todo);

        int databaseSizeBeforeDelete = todoRepository.findAll().size();

        // Delete the todo
        restTodoMockMvc
            .perform(delete(ENTITY_API_URL_ID, todo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Todo> todoList = todoRepository.findAll();
        assertThat(todoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
