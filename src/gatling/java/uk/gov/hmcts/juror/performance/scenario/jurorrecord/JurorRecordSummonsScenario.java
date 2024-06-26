package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.doSwitchOrElse;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.exitHere;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.onCase;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public final class JurorRecordSummonsScenario {

    private static final String GROUP_NAME = "Juror Record Summons";
    private static final String BASE_URL_DIGITAL = "/response/#{juror_number}";
    private static final String BASE_URL_PAPER = "/summons-replies/response/#{juror_number}/paper";

    private JurorRecordSummonsScenario() {

    }

    public static ChainBuilder getSummonsReplyPaper() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " GET - Paper")
            .on(exec(
                    http("GET - Summons Reply - Paper")
                        .get(BASE_URL_PAPER)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("response details"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder getSummonsReplyDigital() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " GET - Digital")
            .on(exec(
                    http("GET - Summons Reply - Digital")
                        .get(BASE_URL_DIGITAL)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("response details"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder getSummonsReply() {
        return
            doSwitchOrElse("#{reply_type}").on(
                onCase("paper").then(getSummonsReplyPaper()),
                onCase("digital").then(getSummonsReplyDigital())
            ).orElse(exitHere());
    }
}
