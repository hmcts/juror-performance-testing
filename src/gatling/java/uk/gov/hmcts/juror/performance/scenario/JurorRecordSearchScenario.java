package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class JurorRecordSearchScenario {
    private static final String GROUP_NAME = "Juror Record Search";


    public static final ChainBuilder JUROR_RECORD_SEARCH = group(GROUP_NAME)
        .on(exec(
            http("POST - Juror Number Search")
                .post("/juror-record/search")
                .headers(Util.COMMON_HEADERS)
                .check(Util.saveCsrf())
                .formParam("super-nav-search", "#{juror_number}")
                .formParam("_csrf", "#{csrf}")
                .check(Util.validatePageIdentifier("juror record - overview"))
                .check(css("#jurorNumber").isEL("#{juror_number}"))
        ))
        ;

    private JurorRecordSearchScenario() {

    }
}
