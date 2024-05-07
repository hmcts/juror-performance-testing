package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public final class JurorRecordSearchScenario {
    private static final String GROUP_NAME = "Juror Record Search";

    private JurorRecordSearchScenario() {

    }

    public static ChainBuilder jurorRecordSearch() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - POST - Juror Number Search")
            .on(exec(
                    http("POST - Juror Number Search")
                        .post("/juror-record/search")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.saveCsrf())
                        .formParam("globalSearch", "#{juror_number}")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("Juror record - Overview"))
                        .check(css("#jurorNumber").isEL("#{juror_number}"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }
}
