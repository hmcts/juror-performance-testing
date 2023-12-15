package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;


public final class JurorRecordUpdateScenario {

    public static final String GROUP_NAME = "Juror Record - Update";

    public static final String BASE_URL = "/juror-management/juror/#{juror_number}/update";


    private JurorRecordUpdateScenario() {

    }

    public static ChainBuilder postUpdateRecordDeferral() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - POST - deferral")
            .on(
                exec(
                    http("POST - Juror Record - Update Record - deferral")
                        .post(JurorRecordUpdateScenario.BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("jurorRecordUpdate", "deferral")
                        .formParam("jurorDeceased", "")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("process - deferral"))
                )
            );
    }

    public static ChainBuilder postUpdateRecordExcusal() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - POST - excusal")
            .on(
                exec(
                    http("POST - Juror Record - Update Record - excusal")
                        .post(JurorRecordUpdateScenario.BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("jurorRecordUpdate", "excusal")
                        .formParam("jurorDeceased", "")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("process - what to do"))
                        .check(Util.validateHeading("Grant or refuse an excusal"))
                )
            );
    }

    public static ChainBuilder postUpdateRecordPostpone() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - POST - postpone")
            .on(
                exec(
                    http("POST - Juror Record - Update Record - postpone")
                        .post(JurorRecordUpdateScenario.BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("jurorRecordUpdate", "postpone")
                        .formParam("jurorDeceased", "")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("Update Juror Record - Postpone"))
                        .check(css("#postponeTo", "data-mindate").exists().saveAs("postponeNewServiceStartMinDateStr"))
                )
            );
    }
}
