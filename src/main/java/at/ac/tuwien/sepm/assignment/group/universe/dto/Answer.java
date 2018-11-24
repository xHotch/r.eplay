package at.ac.tuwien.sepm.assignment.group.universe.dto;

import java.util.Objects;

public class Answer {

    private Long id;
    private String text;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Answer(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Answer answer = (Answer) o;
        return Objects.equals(id, answer.id) &&
            Objects.equals(text, answer.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }

    @Override
    public String toString() {
        return "Answer{" +
            "id=" + id +
            ", text='" + text + '\'' +
            '}';
    }
}
