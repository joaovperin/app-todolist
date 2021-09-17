package br.jpe.tdl.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link br.jpe.tdl.domain.Todo} entity. This class is used
 * in {@link br.jpe.tdl.web.rest.TodoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /todos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class TodoCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter subject;

    private StringFilter detail;

    private Boolean distinct;

    public TodoCriteria() {}

    public TodoCriteria(TodoCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.subject = other.subject == null ? null : other.subject.copy();
        this.detail = other.detail == null ? null : other.detail.copy();
        this.distinct = other.distinct;
    }

    @Override
    public TodoCriteria copy() {
        return new TodoCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getSubject() {
        return subject;
    }

    public StringFilter subject() {
        if (subject == null) {
            subject = new StringFilter();
        }
        return subject;
    }

    public void setSubject(StringFilter subject) {
        this.subject = subject;
    }

    public StringFilter getDetail() {
        return detail;
    }

    public StringFilter detail() {
        if (detail == null) {
            detail = new StringFilter();
        }
        return detail;
    }

    public void setDetail(StringFilter detail) {
        this.detail = detail;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TodoCriteria that = (TodoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(subject, that.subject) &&
            Objects.equals(detail, that.detail) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subject, detail, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TodoCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (subject != null ? "subject=" + subject + ", " : "") +
            (detail != null ? "detail=" + detail + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
