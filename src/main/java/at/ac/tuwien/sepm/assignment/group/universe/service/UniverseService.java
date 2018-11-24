package at.ac.tuwien.sepm.assignment.group.universe.service;

import at.ac.tuwien.sepm.assignment.group.universe.dto.Answer;
import at.ac.tuwien.sepm.assignment.group.universe.exceptions.ServiceException;

/**
 * The <code>UniverseService</code> is capable to calculate the answer to
 * <blockquote>Ultimate Question of Life, the Universe, and Everything</blockquote>.
 * Depending on the implementation it might take a while.
 */
public interface UniverseService {

    /**
     * Calculate the answer to the ultimate question of life, the universe, and everything.
     *
     * @return the answer to the ultimate question question
     * @throws ServiceException in case the method does not complete successfully
     */
    Answer calculateAnswer() throws ServiceException;

}
