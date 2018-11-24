package at.ac.tuwien.sepm.assignment.group.universe.dao;

import at.ac.tuwien.sepm.assignment.group.universe.dto.Answer;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Question;
import at.ac.tuwien.sepm.assignment.group.universe.exceptions.PersistenceException;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class JDBCUniverseDAO implements UniverseDAO {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String SELECT_ANSWER_WHERE_QUESTION_LIKE = "SELECT id, answer FROM qanda WHERE question = ?";

    private final JDBCConnectionManager jdbcConnectionManager;

    public JDBCUniverseDAO(JDBCConnectionManager jdbcConnectionManager) {
        this.jdbcConnectionManager = jdbcConnectionManager;
    }

    @Override
    public Answer getAnswerToQuestion(Question question) throws PersistenceException {
        try (
            Connection connection = jdbcConnectionManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(SELECT_ANSWER_WHERE_QUESTION_LIKE);
        ) {
            ps.setString(1, question.getText());
            try (ResultSet resultSet = ps.executeQuery()) {

                if (resultSet.next()) {
                    return new Answer(
                        resultSet.getLong("id"),
                        resultSet.getString("answer")
                    );
                } else {
                    throw new PersistenceException("Could not find expected answer");
                }
            }

        } catch (SQLException e) {
            throw new PersistenceException("Could not execute database query", e);
        }
    }
}
