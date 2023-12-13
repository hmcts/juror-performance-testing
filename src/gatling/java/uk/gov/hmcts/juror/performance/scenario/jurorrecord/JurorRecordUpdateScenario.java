package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;

import java.time.format.DateTimeFormatter;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;

public class JurorRecordUpdateScenario {

    private static final String GROUP_NAME = "Juror Record - Update";

    public static final String BASE_URL = "/juror-management/juror/#{juror_number}/update";

    private static final DateTimeFormatter deferralDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static final ChainBuilder POST_UPDATE_RECORD_DEFERRAL = group(GROUP_NAME)
        .on(
            exec(
                http("POST - Juror Record - Update Record - deferral")
                    .get(JurorRecordUpdateScenario.BASE_URL)
                    .headers(Util.COMMON_HEADERS)
                    .formParam("jurorRecordUpdate", "deferral")
                    .formParam("jurorDeceased", "")
                    .formParam("_csrf", "#{csrf}")
                    .check(Util.validatePageIdentifier("process - deferral"))
            )
        );

    public static class Deferral {
        public static final ChainBuilder POST_DEFERAL_GRANT = group(GROUP_NAME)
            .on(
                feed(Feeders.DEFERAL_CODE_FEEDER)
                    .exec(
                    http("POST - Juror Record - Update Record - deferral - GRANT")
                        .get(JurorRecordUpdateScenario.BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("deferralReason", "#{exc_code}")
                        .formParam("deferralDecision", "GRANT")
                        .formParam("deferralDateSelection", "otherDate")
                        .formParam("deferralDate", Util.createDateString(deferralDateFormatter))
                        .formParam("hearingDate", "")
                        .formParam("jurorNumber", "#{juror_number}")
                        .formParam("_csrf", "#{csrf}")
                        .formParam("version", "")
                        .check(Util.validatePageIdentifier("juror record - overview"))
                        .check(substring("Deferral granted"))
                )
            );
        public static final ChainBuilder POST_DEFERAL_REFUSE = group(GROUP_NAME)
            .on(
                feed(Feeders.DEFERAL_CODE_FEEDER)
                    .exec(
                        http("POST - Juror Record - Update Record - deferral - REFUSE")
                            .get(JurorRecordUpdateScenario.BASE_URL)
                            .headers(Util.COMMON_HEADERS)
                            .formParam("deferralReason", "#{exc_code}")
                            .formParam("deferralDecision", "REFUSE")
                            .formParam("deferralDate", "")
                            .formParam("hearingDate", "")
                            .formParam("jurorNumber", "#{juror_number}")
                            .formParam("_csrf", "#{csrf}")
                            .formParam("version", "")
                            .check(Util.validatePageIdentifier("juror record - overview"))
                            .check(substring("Deferral granted"))
                    )
            );
    }
}
