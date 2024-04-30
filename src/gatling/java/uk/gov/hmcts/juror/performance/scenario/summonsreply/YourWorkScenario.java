package uk.gov.hmcts.juror.performance.scenario.summonsreply;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class YourWorkScenario {
    private static final String GROUP_NAME = "Your work";
    private static final String TODO_URL = "/inbox";
    private static final String AWAITING_INFORMATION_URL = "/pending";
    private static final String COMPLETED_URL = "/completed";


    public static ChainBuilder getToDo() {
        String scenarioId = Util.getNewScenarioId();
        return group(scenarioId + GROUP_NAME + " - GET - To do")
            .on(
                exec(
                    http("GET - Your work - to do")
                        .get(TODO_URL)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("your work - to do"))
                )
            ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS));
    }

    public static ChainBuilder getAwaitingInformation() {
        String scenarioId = Util.getNewScenarioId();
        return group(scenarioId + GROUP_NAME + " - GET - awaiting information")
            .on(
                exec(
                    http("GET - Your work - awaiting information")
                        .get(AWAITING_INFORMATION_URL)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("your work - awaiting information"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))

            );
    }

    public static ChainBuilder getCompleted() {
        String scenarioId = Util.getNewScenarioId();
        return group(scenarioId + GROUP_NAME + " - GET - completed")
            .on(
                exec(
                    http("GET - Your work - completed")
                        .get(COMPLETED_URL)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("your work - completed"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }
}
