package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyPaperScenario;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;

public class JurorRecordSummonsScenario {

    private static final String GROUP_NAME = "Juror Record Summons";


    public static ChainBuilder GET_SUMMONS_REPLY_PAPER = group(GROUP_NAME)
        .on(exec(
                http("GET - Summons Reply - Paper")
                    .get(SummonsReplyPaperScenario.BASE_URL)
                    .headers(Util.COMMON_HEADERS)
                    .check(Util.validatePageIdentifier("response details"))
            ).exitHereIfFailed()
        );

}
