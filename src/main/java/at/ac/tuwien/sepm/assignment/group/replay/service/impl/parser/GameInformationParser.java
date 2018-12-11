package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import com.jayway.jsonpath.ReadContext;
import org.springframework.stereotype.Service;

@Service
class GameInformationParser {


    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    private ReadContext ctx;

}
