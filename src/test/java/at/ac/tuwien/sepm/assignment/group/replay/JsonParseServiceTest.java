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
    private File goodReplay;

    @Before
    public void setUp() {
        jsonFile = new File(getClass().getResource("/testJson/kurzesreplay.json").getFile());
        badJsonFile = new File(getClass().getResource("/testJson/keinreplay.json").getFile());
        goodReplay = new File(getClass().getResource("/testJson/goodReplay.json").getFile());
    }

    @Test
    public void testParseMatchReturnsCorrectMatchDto() {
        JsonParseService jps = new JsonParseServiceJsonPath();
        try {
            MatchDTO match = jps.parseMatch(goodReplay);
            Assert.assertThat(match.getTeamSize(), is(3));
            Assert.assertThat(match.getReadId(), is("2F2200D4435F2EF5691E298320832A4B"));
            Assert.assertThat(match.getDateTime(), is(LocalDateTime.of(2018, 12, 2, 17, 7, 28)));
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
