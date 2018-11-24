package at.ac.tuwien.sepm.assignment.group.universe.dto;

import java.util.Objects;

public class Question {

    private String text;

    public Question(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(text, question.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "Question{" +
            "text='" + text + '\'' +
            '}';
    }
}
