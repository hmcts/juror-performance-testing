package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.PrintLetterScenario;
import uk.gov.hmcts.juror.support.generation.generators.code.Generator;
import uk.gov.hmcts.juror.support.generation.generators.value.RandomFromCollectionGeneratorImpl;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class JurorRecordExcusalScenario {
    private static final String GROUP_NAME = JurorRecordUpdateScenario.GROUP_NAME + " - excusal";
    private static final String BASE_URL = JurorRecordUpdateScenario.BASE_URL + "/excusal";

    public static ChainBuilder postExcusalGrant() {
        return postExcusalGrant(Util.getNewScenarioId());
    }

    public static ChainBuilder postExcusalGrant(String scenarioId) {
        return Util.group(scenarioId + GROUP_NAME + " - POST - GRANT")
            .on(
                feed(Feeders.EXCUSAL_CODE_FEEDER)
                    .exec(
                        http("POST - Juror Record - Update Record - Excusal - GRANT")
                            .post(BASE_URL)
                            .headers(Util.COMMON_HEADERS)
                            .formParam("excusalCode", "#{exc_code}")
                            .formParam("excusalDecision", "GRANT")
                            .formParam("_csrf", "#{csrf}")
                            .check(Util.validatePageIdentifier("process - excusal"))
                            .check(substring("Do you want to print an excusal granted letter?"))
                    ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
                    .exec(PrintLetterScenario.postPrintLetter(
                        "POST - Juror Record - Update Record - Excusal - GRANT",
                        "excusal-granted",
                        "excusal/grant",
                        "Excusal granted",
                        "process - excusal"
                    ))
            );
    }

    public static ChainBuilder postExcusalRefuse() {
        return postExcusalRefuse(Util.getNewScenarioId());
    }

    public static ChainBuilder postExcusalRefuse(String scenarioId) {
        return Util.group(scenarioId + GROUP_NAME + " - POST -  REFUSE")
            .on(
                feed(Feeders.EXCUSAL_CODE_FEEDER)
                    .exec(
                        http("POST - Juror Record - Update Record - Excusal - REFUSE")
                            .post(BASE_URL)
                            .headers(Util.COMMON_HEADERS)
                            .formParam("excusalCode", "#{exc_code}")
                            .formParam("excusalDecision", "REFUSE")
                            .formParam("_csrf", "#{csrf}")
                            .check(Util.validatePageIdentifier("Juror record - Overview"))
                            .check(substring("Excusal refused"))
                    ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }
}
