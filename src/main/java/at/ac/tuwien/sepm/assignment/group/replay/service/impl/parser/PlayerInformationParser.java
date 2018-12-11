package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
class PlayerInformationParser {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ReadContext ctx;


    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }


}
