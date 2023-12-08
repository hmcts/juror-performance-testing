package uk.gov.hmcts.juror.performance.scenario.summonsreply;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;

public class SummonsReplyPaperScenario {

    private static final String GROUP_NAME = "Summons Reply - Paper";
    public static final String BASE_URL = "/summons-replies/response/#{juror_number}/paper";


    public static ChainBuilder GET_PROCESS_REPLY = group(GROUP_NAME)
        .on(exec(
                http("GET - Summons Reply - Process Reply")
                    .get(BASE_URL + "/process")
                    .headers(Util.COMMON_HEADERS)
                    .check(Util.validatePageIdentifier("process - what to do"))
            ).exitHereIfFailed()
        );
}
