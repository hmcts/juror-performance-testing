package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.PrintLetterScenario;

import java.time.Duration;
import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class JurorRecordDeferralScenario {
    private static final String GROUP_NAME = JurorRecordUpdateScenario.GROUP_NAME + " - deferral";
    private static final String BASE_URL = JurorRecordUpdateScenario.BASE_URL + "/deferral";

    public static ChainBuilder postDeferalGrant() {
        return postDeferalGrant(Util.getNewScenarioId());
    }


    public static ChainBuilder postDeferalGrant(String scenarioId) {
        return Util.group(scenarioId + GROUP_NAME + " - POST - GRANT")
            .on(
                feed(Feeders.DEFERAL_CODE_FEEDER)
                    .exec(session -> {
                        LocalDate localDate = Util.createDate();
                        return session
                            .set("deferralDate", Util.STANDARD_DATE_FORMATTER.format(localDate))
                            .set("hearingDate", Util.STANDARD_DATE_FORMATTER.format(localDate.minusDays(1)));
                    })
                    .exec(
                        http("POST - Juror Record - Update Record - deferral - GRANT")
                            .post(BASE_URL)
                            .headers(Util.COMMON_HEADERS)
                            .formParam("deferralReason", "#{exc_code}")
                            .formParam("deferralDecision", "GRANT")
                            .formParam("deferralDateSelection", "otherDate")
                            .formParam("deferralDate","#{deferralDate}")
                            .formParam("hearingDate", "#{hearingDate}")
                            .formParam("jurorNumber", "#{juror_number}")
                            .formParam("_csrf", "#{csrf}")
                            .formParam("version", "")
                            .check(Util.validatePageIdentifier("process - deferral"))
                            .check(substring("Do you want to print a deferral granted letter?"))
                            .check(Util.saveCsrf())
                    ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
                    .exec(PrintLetterScenario.postPrintLetter(
                        "POST - Juror Record - Update Record - deferral - GRANT",
                        "deferral-granted",
                        "deferral/grant",
                        "Deferral granted",
                        "process - deferral"
                    ))
            );
    }


    public static ChainBuilder postDeferalRefuse() {
        return postDeferalRefuse(Util.getNewScenarioId());
    }

    public static ChainBuilder postDeferalRefuse(String scenarioId) {
        return Util.group(scenarioId + GROUP_NAME + " - POST - REFUSE")
            .on(
                feed(Feeders.DEFERAL_CODE_FEEDER)
                    .exec(
                        http("POST - Juror Record - Update Record - deferral - REFUSE")
                            .post(BASE_URL)
                            .headers(Util.COMMON_HEADERS)
                            .formParam("deferralReason", "#{exc_code}")
                            .formParam("deferralDecision", "REFUSE")
                            .formParam("deferralDate", "")
                            .formParam("hearingDate", "")
                            .formParam("jurorNumber", "#{juror_number}")
                            .formParam("_csrf", "#{csrf}")
                            .formParam("version", "")
                            .check(Util.validatePageIdentifier("Juror record - Overview"))
                            .check(substring("Deferral refused"))
                    ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }
}
