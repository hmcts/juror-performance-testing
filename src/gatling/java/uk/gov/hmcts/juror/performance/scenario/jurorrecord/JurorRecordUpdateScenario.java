package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;

import java.time.format.DateTimeFormatter;

import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.exitHere;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;


public final class JurorRecordUpdateScenario {

    private static final String GROUP_NAME = "Juror Record - Update";

    public static final String BASE_URL = "/juror-management/juror/#{juror_number}/update";

    private static final DateTimeFormatter deferralDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        return exitHere();//TODO
    }

    public static class Deferral {
        public static ChainBuilder postDeferalGrant() {
            return postDeferalGrant(Util.getNewScenarioId());
        }

        public static ChainBuilder postDeferalGrant(String scenarioId) {
            return group(scenarioId + GROUP_NAME + " - POST - deferral - GRANT")
                .on(
                    feed(Feeders.DEFERAL_CODE_FEEDER).exec(
                        http("POST - Juror Record - Update Record - deferral - GRANT")
                            .post(JurorRecordUpdateScenario.BASE_URL + "/deferral")
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
        }

        public static ChainBuilder postDeferalRefuse() {
            return postDeferalRefuse(Util.getNewScenarioId());
        }

        public static ChainBuilder postDeferalRefuse(String scenarioId) {
            return group(scenarioId + GROUP_NAME + " - POST - deferral - REFUSE")
                .on(
                    feed(Feeders.DEFERAL_CODE_FEEDER)
                        .exec(
                            http("POST - Juror Record - Update Record - deferral - REFUSE")
                                .post(JurorRecordUpdateScenario.BASE_URL + "/deferral")
                                .headers(Util.COMMON_HEADERS)
                                .formParam("deferralReason", "#{exc_code}")
                                .formParam("deferralDecision", "REFUSE")
                                .formParam("deferralDate", "")
                                .formParam("hearingDate", "")
                                .formParam("jurorNumber", "#{juror_number}")
                                .formParam("_csrf", "#{csrf}")
                                .formParam("version", "")
                                .check(bodyString())
                                .check(Util.validatePageIdentifier("juror record - overview"))
                                .check(substring("Deferral refused"))
                        )
                );
        }
    }

    public static class Excusal {

        public static ChainBuilder postExcusalGrant() {
            return postExcusalGrant(Util.getNewScenarioId());
        }
        public static ChainBuilder postExcusalGrant(String newScenarioId) {
            return exitHere();//TODO
        }

        public static ChainBuilder postExcusalRefuse() {
            return postExcusalRefuse(Util.getNewScenarioId());
        }
        public static ChainBuilder postExcusalRefuse(String newScenarioId) {
            return exitHere();//TODO
        }
    }
}
