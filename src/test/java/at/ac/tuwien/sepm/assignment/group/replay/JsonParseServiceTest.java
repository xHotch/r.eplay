package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.BallStatistic;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.PlayerStatistic;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.RigidBodyStatistic;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class JsonParseServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private File jsonFile;
    private File badJsonFile;
    private File goodReplay;


    private JsonParseService jsonParseService;
    private RigidBodyParser rigidBodyParser;
    private BallInformationParser ballInformationParser;
    private CarInformationParser carInformationParser;
    private BoostInformationParser boostInformationParser;
    private GameInformationParser gameInformationParser;
    private PlayerInformationParser playerInformationParser;

    private PlayerStatistic playerStatistic;
    private BallStatistic ballStatistic;
    private RigidBodyStatistic rigidBodyStatistic;





    @Before
    public void setUp() {
        jsonFile = new File(getClass().getResource("/testJson/kurzesreplay.json").getFile());
        badJsonFile = new File(getClass().getResource("/testJson/keinreplay.json").getFile());
        goodReplay = new File(getClass().getResource("/testJson/goodReplay.json").getFile());


        rigidBodyParser = new RigidBodyParser();
        ballInformationParser = new BallInformationParser(rigidBodyParser);
        carInformationParser = new CarInformationParser(rigidBodyParser);
        boostInformationParser = new BoostInformationParser(rigidBodyParser);
        gameInformationParser = new GameInformationParser();
        playerInformationParser = new PlayerInformationParser();

        rigidBodyStatistic = new RigidBodyStatistic();
        ballStatistic = new BallStatistic(rigidBodyStatistic);
        playerStatistic = new PlayerStatistic(rigidBodyStatistic);

        jsonParseService = new JsonParseServiceJsonPath(rigidBodyParser,playerInformationParser,gameInformationParser,carInformationParser,ballInformationParser,boostInformationParser,playerStatistic,ballStatistic,rigidBodyStatistic);

    }



    @Test
    public void testParseMatchReturnsCorrectMatchDto() {
        try {
            MatchDTO match = jsonParseService.parseMatch(goodReplay);
            Assert.assertThat(match.getTeamSize(), is(3));
            Assert.assertThat(match.getReadId(), is("2F2200D4435F2EF5691E298320832A4B"));
            Assert.assertThat(match.getDateTime(), is(LocalDateTime.of(2018, 12, 2, 17, 7, 28)));

            List<MatchPlayerDTO> matchPlayerDTOS = match.getPlayerData();

            boolean playerFound = false;
            for (MatchPlayerDTO matchPlayerDTO : matchPlayerDTOS){
                if (matchPlayerDTO.getPlayerDTO().getPlatformID()==(3533021571419362446L)) {
                    playerFound=true;
                    Assert.assertThat(matchPlayerDTO.getGoals(),is(1));
                }
            }

            Assert.assertThat(playerFound,is(true));
            testMaps();



        } catch (FileServiceException e) {
            fail();
        }
    }

    @Test(expected = FileServiceException.class)
    public void testParseMatchThrowsExceptionWhenUsedWithBadJson() throws FileServiceException {
        jsonParseService.parseMatch(badJsonFile);
    }


    private void testMaps(){
        LinkedHashMap<Integer, Integer> playerCarMap = carInformationParser.getPlayerCarMap();

        LinkedHashMap<Integer, List<RigidBodyInformation>> rigidBodyMap = carInformationParser.getRigidBodyMap();

        long mapSize = playerCarMap.size();
        Assert.assertEquals(rigidBodyMap.size(),mapSize);
    }


}
