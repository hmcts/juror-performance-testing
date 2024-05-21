package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;


public final class JurorRecordUpdateScenario {

    public static final String GROUP_NAME = "Juror Record - Update";

    public static final String BASE_URL = "/juror-management/juror/#{juror_number}/update";


    private JurorRecordUpdateScenario() {

    }

    public static ChainBuilder postUpdateRecordDeferral() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - POST - deferral")
            .on(
                exec(
                    http("POST - Juror Record - Update Record - deferral")
                        .post(JurorRecordUpdateScenario.BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("jurorRecordUpdate", "deferral")
                        .formParam("jurorDeceased", "")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("Process - Deferral"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder postUpdateRecordExcusal() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - POST - excusal")
            .on(
                exec(
                    http("POST - Juror Record - Update Record - excusal")
                        .post(JurorRecordUpdateScenario.BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("jurorRecordUpdate", "excusal")
                        .formParam("jurorDeceased", "")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("Process - what to do"))
                        .check(Util.validateHeading("Grant or refuse an excusal"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder postUpdateRecordPostpone() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - POST - postpone")
            .on(
                exec(
                    http("POST - Juror Record - Update Record - postpone")
                        .post(JurorRecordUpdateScenario.BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("jurorRecordUpdate", "postpone")
                        .formParam("jurorDeceased", "")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("Update juror record - Postpone"))
                        .check(css("#postponeTo", "data-mindate").exists().saveAs("postponeNewServiceStartMinDateStr"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }
}
