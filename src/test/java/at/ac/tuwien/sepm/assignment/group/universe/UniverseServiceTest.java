package at.ac.tuwien.sepm.assignment.group.universe;

import at.ac.tuwien.sepm.assignment.group.universe.dao.UniverseDAO;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Answer;
import at.ac.tuwien.sepm.assignment.group.universe.service.SimpleUniverseService;
import at.ac.tuwien.sepm.assignment.group.universe.service.UniverseService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UniverseServiceTest {

    @Mock
    private UniverseDAO mockUniverseDAO;

    private UniverseService universeService;

    @Before
    public void setUp() throws Exception {
        when(mockUniverseDAO.getAnswerToQuestion(any())).thenReturn(new Answer(1L, "42"));

        universeService = new SimpleUniverseService(mockUniverseDAO);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testSimpleUniverseService() throws Exception {
        Assert.assertThat(universeService.calculateAnswer().getText(), is("42"));
    }
}
