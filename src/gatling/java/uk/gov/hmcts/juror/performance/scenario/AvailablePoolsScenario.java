package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.support.generation.generators.value.RandomFromCollectionGeneratorImpl;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class AvailablePoolsScenario {
    private static final String GROUP_NAME = "Available Pools";
    private static final String BASE_URL = "/juror-management/juror/#{juror_number}/available-pools";


    public static ChainBuilder postDeferralMaintenance() {
        return postDeferralMaintenance(Util.getNewScenarioId());
    }

    public static ChainBuilder postDeferralMaintenance(String scenarioId) {
        return postPoolRequestInternal(scenarioId, "Deferral Maintenance");
    }

    public static HttpRequestActionBuilder applyChecks(HttpRequestActionBuilder http) {
        return http
            //Validates we are on either the select a pool for this date or no active pools screen
            .check(css("h1.govuk-heading-l")
                .transform(value -> {
                    if (value != null && (
                        value.equals("There are no active pools for this date")
                            || value.equals("Select a pool for this date"))
                    ) {
                        return value;
                    }
                    return null;
                }).notNull().saveAs("headingValue"))
            //If we are on select pool screen sae all the shown pools for later use
            .checkIf(session -> {
                String headingValue = session.getString("headingValue");
                return headingValue != null
                    && headingValue.equals("Select a pool for this date");
            }).then(css("input[name=\"deferralDateAndPool\"]", "value")
                .findAll().notNull().saveAs("deferralDateAndPools")
            );
    }

    public static ChainBuilder postPoolRequest() {
        return postPoolRequest(Util.getNewScenarioId());
    }

    public static ChainBuilder postPoolRequest(String scenarioId) {
        return exec(session -> session.set("deferralDateAndPoolDate",
            new RandomFromCollectionGeneratorImpl<>(session.getList("deferralDateAndPools"))
                .generate()))
            .exec(postPoolRequestInternal(scenarioId, "Pool Request"));
    }

    public static ChainBuilder postPoolRequestInternal(String scenarioId, String name) {
        return Util.group(scenarioId + GROUP_NAME + " - POST - " + name)
            .on(exec(
                    http("POST - Available pools - " + name)
                        .post(BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("deferralDateAndPool", "#{deferralDateAndPoolDate}")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("process - postpone"))
                        .check(substring("Do you want to print a postponement letter?"))
                        .check(Util.saveCsrf())
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
                    .exec(PrintLetterScenario.postPrintLetter(
                        "POST - Available pools - " + name,
                        "postponement",
                        "postpone",
                        "Juror record updated: <b>Postponed</b>",
                        "process - postpone"
                    ))
            );
    }
}
