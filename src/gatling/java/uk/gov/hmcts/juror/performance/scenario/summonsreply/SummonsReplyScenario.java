package uk.gov.hmcts.juror.performance.scenario.summonsreply;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public final class SummonsReplyScenario {

    private static final String GROUP_NAME = "Summons Reply - #{reply_type}";
    public static final String BASE_URL = "/summons-replies/response/#{juror_number}/#{reply_type}";

    private SummonsReplyScenario() {

    }

    public static ChainBuilder getProcessReply() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Process Reply")
            .on(exec(
                    http("GET - Summons Reply - Process Reply")
                        .get(BASE_URL + "/process")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("process - what to do"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }
}
