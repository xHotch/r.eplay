package at.ac.tuwien.sepm.assignment.group.universe;

import at.ac.tuwien.sepm.assignment.group.universe.dao.UniverseDAO;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Answer;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Question;
import at.ac.tuwien.sepm.assignment.group.universe.exceptions.PersistenceException;
import at.ac.tuwien.sepm.assignment.group.universe.service.SimpleUniverseService;
import at.ac.tuwien.sepm.assignment.group.universe.service.UniverseService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * - @RunWith(MockitoJUniRunner.class) annotation makes sure to populate all fields annotated with @Mock.
 * @author Elias Brugger
 */
@RunWith(MockitoJUnitRunner.class)
public class TestTemplate {

    /**
     * - @Mock annotation creates a mock object for the specified class or interface.
     * Simulates the definition of an object and populates it.
     */
    @Mock
    private UniverseDAO mockUniverseDAO;

    private UniverseService universeService;

    /**
     * A method annotated with @Before gets called before each test annotated with @Test.
     * setUp() should include everything that needs to be set up so the test method runs with
     * the needed resources.
     * @throws Exception throws an exception when anything fails while setting up the resources.
     */
    @Before
    public void setUp() throws Exception {

        /*
         * when().thenReturn() defines the expected value of a method call
         * usage: when(methodCall).thenReturn(expectedValue)
         *
         * any(), anyInt(), anyString() ... is used to define a certain return type from the called method, independent of the input.
         * any() is used for any Objects
         * anyInt() is used for Integer
         * anyString() is used for Strings
         * ...
         */
        when(mockUniverseDAO.getAnswerToQuestion(any())).thenReturn(new Answer(1L, "42"));


        /*
         * Passes over the mocked UniverseDAO to the SimpleUniverseService.
         */
        universeService = new SimpleUniverseService(mockUniverseDAO);
    }

    /**
     * A method annotated with @After gets called after each test annotated with @Test.
     * tearDown() should include everything that needs to be done to restore all things after
     * running a test class.
     * e.g. remove items from the database after populating it for the test run.
     */
    @After
    public void tearDown() {

    }

    /**
     * This is an example method that demonstrates the usage for spy().
     * spy() wraps a real object, method calls and return values to the spy() object can be defined.
     */
    @Test
    public void SpyDemonstration() {

        /*
         * create a linked list, also create a spy that can be used to manipulate the outcome of a method call.
         */
        List<String> list = new LinkedList<>();

        /*
         * wrap the 'list' variable with a spy.
         */
        List<String> spy = spy(list);

        /*
         * define the return value when spy.get(0) is called so it does not throw an exception.
         * use 'doReturn(value).when(spy).methodCall()', specifies a return value for a specific method call.
         */
        doReturn("42").when(spy).get(0);

        /*
         * assert that when spy.get(0) is called, "42" is returned.
         */
        Assert.assertThat("42", is(spy.get(0)));

    }

    /**
     * Mockito tracks all the method calls and their parameters to the mock object.
     * verify() verifies if specified conditions were met. (behaviour testing)
     * verify() does not check the result of a method call.
     */
    @Test
    public void VerifyDemonstration() {

        /*
         * create a mocked UniverseDAO
         */
        UniverseDAO test = mock(UniverseDAO.class);

        /*
         *
         */
        try {
            when(test.getAnswerToQuestion(any())).thenReturn(new Answer(1L, "42"));
        } catch (PersistenceException e) {

        }

        /*
         * check if list.get(0) was called 3 times, and check if one method call was passed a given question.
         */
        try {
            /*
             * general usage: verify(MockObject, optional: how many times invoked).methodCall(eq(value))
             */

            // first method call
            test.getAnswerToQuestion(new Question("Answer to the Ultimate Question of Life, the Universe, and Everything"));

            /*
             * checks if the method call's passed argument the given question. It checks equality with 'eq(argument)' (Argument matcher).
             */
            verify(test).getAnswerToQuestion(eq(new Question("Answer to the Ultimate Question of Life, the Universe, and Everything")));

            // call some methods again
            test.getAnswerToQuestion(new Question("Answer to the Ultimate Question of Life, the Universe, and Everything"));
            test.getAnswerToQuestion(new Question("Answer to the Ultimate Question of Life, the Universe, and Everything"));

            /*
             * checks how many times a method was called, there are more options available, only times() and atMost()
             * are listed here.
             */
            verify(test, times(3)).getAnswerToQuestion(any());
            verify(test, atMost(4)).getAnswerToQuestion(any());
            verify(test, atLeast(2)).getAnswerToQuestion(any());

            // checks if no more calls were made on this object after the calls above.
            verifyNoMoreInteractions(test);

        } catch (PersistenceException e) {

        }

    }

    @Test
    public void testSimpleUniverseService() throws Exception {
        Assert.assertThat(universeService.calculateAnswer().getText(), is("42"));
    }
}
