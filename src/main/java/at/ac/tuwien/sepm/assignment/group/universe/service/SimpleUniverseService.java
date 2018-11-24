package at.ac.tuwien.sepm.assignment.group.universe.service;

import at.ac.tuwien.sepm.assignment.group.universe.dao.UniverseDAO;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Answer;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Question;
import at.ac.tuwien.sepm.assignment.group.universe.exceptions.PersistenceException;
import at.ac.tuwien.sepm.assignment.group.universe.exceptions.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class SimpleUniverseService implements UniverseService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String THE_QUESTION = "question of life, the universe, and everything";

    private final UniverseDAO universeDAO;

    public SimpleUniverseService(UniverseDAO universeDAO) {
        this.universeDAO = universeDAO;
    }


    @Override
    public Answer calculateAnswer() throws ServiceException {
        LOG.debug("called calculateAnswer");

        try {
            return universeDAO.getAnswerToQuestion(new Question(THE_QUESTION));
        } catch (PersistenceException e) {
            throw new ServiceException("Could not compute the answer", e);
        }
    }

}
