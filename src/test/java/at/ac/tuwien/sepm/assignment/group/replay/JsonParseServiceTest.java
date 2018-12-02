package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseServiceJsonPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.time.LocalDateTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class JsonParseServiceTest {

    private File jsonFile;
    private File badJsonFile;

    @Before
    public void setUp() {
        jsonFile = new File(getClass().getResource("/testJson/kurzesreplay.json").getFile());
        badJsonFile = new File(getClass().getResource("/testJson/keinreplay.json").getFile());
    }

    @Test
    public void testParseMatchReturnsCorrectMatchDto() {
        JsonParseService jps = new JsonParseServiceJsonPath();
        try {
            MatchDTO match = jps.parseMatch(jsonFile);
            Assert.assertThat(match.getTeamSize(), is(2));
            Assert.assertThat(match.getReadId(), is("8F9288E34524733E26DDBEA88E33A0F9"));
            Assert.assertThat(match.getDateTime(), is(LocalDateTime.of(2018, 10, 8, 21, 28, 38)));
        } catch (FileServiceException e) {
            fail();
        }
    }
    @Test(expected = FileServiceException.class)
    public void testParseMatchThrowsExceptionWhenUsedWithBadJson() throws FileServiceException {
        JsonParseService jps = new JsonParseServiceJsonPath();
        jps.parseMatch(badJsonFile);
    }


}
