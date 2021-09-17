package br.jpe.tdl.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link br.jpe.tdl.domain.Todo} entity.
 */
public class TodoDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 50)
    private String subject;

    private String detail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TodoDTO)) {
            return false;
        }

        TodoDTO todoDTO = (TodoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, todoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TodoDTO{" +
            "id=" + getId() +
            ", subject='" + getSubject() + "'" +
            ", detail='" + getDetail() + "'" +
            "}";
    }
}
