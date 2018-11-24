package at.ac.tuwien.sepm.assignment.group.universe.dao;


import at.ac.tuwien.sepm.assignment.group.universe.dto.Answer;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Question;
import at.ac.tuwien.sepm.assignment.group.universe.exceptions.PersistenceException;

public interface UniverseDAO {

    Answer getAnswerToQuestion(Question question) throws PersistenceException;
}
