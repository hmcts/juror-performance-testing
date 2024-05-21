package uk.gov.hmcts.juror.performance.scenario.summonsreply;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class SummonsReplySearchScenario {
    private static final String GROUP_NAME = "Search";
    private static final String SEARCH_URL = "/search";


    public static ChainBuilder getSearch() {
        String scenarioId = Util.getNewScenarioId();
        return Util.group(scenarioId + GROUP_NAME + " - POST - search")
            .on(
                exec(
                    http("GET - Search")
                        .get(SEARCH_URL)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("Search"))
                        .check(Util.saveCsrf())
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    private static ChainBuilder resetSearchCriteria() {
        return exec(
            session -> session
                .set("search_juror_number", "")
                .set("search_juror_last_name", "")
                .set("search_pool_number", "")
        );
    }

    public static ChainBuilder postSearchJurorNumber() {
        return exec(resetSearchCriteria()).exec(
                session -> session.set("search_juror_number", session.get("juror_number")))
            .exec(postSearch("Juror Number"));
    }

    public static ChainBuilder postSearchLastName() {
        return exec(resetSearchCriteria()).exec(
                session -> session.set("search_juror_last_name", session.get("last_name")))
            .exec(postSearch("Last Name"));
    }

    public static ChainBuilder postSearchPoolNumber() {
        return exec(resetSearchCriteria()).exec(
                session -> session.set("search_pool_number", session.get("pool_number")))
            .exec(postSearch("Pool Number"));
    }

    public static ChainBuilder postSearch(String suffix) {
        String scenarioId = Util.getNewScenarioId();
        return Util.group(scenarioId + GROUP_NAME + " - POST - search")
            .on(exec(
                    http("POST - Search - " + suffix)
                        .post(SEARCH_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("juror_number", "#{search_juror_number}")
                        .formParam("last_name", "#{search_juror_last_name}")
                        .formParam("pool_number", "#{search_pool_number}")
                        .formParam("_csrf", "#{csrf}")
                        .check(status().is(200))
                        .check(Util.validatePageIdentifier("Search"))
                        //Checks the no results found box is hidden
                        .check(css("#noResultsMsg.u-hide").exists())
                        //Checks I can see results
                        .check(css("#selectedSendToForm>#searchResultTable>tbody>tr").count().gte(1))
                        .check(css("#searchSummaryMsg.u-hide").notExists())
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

}
