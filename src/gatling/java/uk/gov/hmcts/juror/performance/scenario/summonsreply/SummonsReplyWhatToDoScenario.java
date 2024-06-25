package uk.gov.hmcts.juror.performance.scenario.summonsreply;

import io.gatling.javaapi.core.ChainBuilder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

@Slf4j
public final class SummonsReplyWhatToDoScenario {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter deferralDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String GROUP_NAME = "Summons Reply - What To Do";
    public static final String BASE_URL = SummonsReplyScenario.BASE_URL;

    private SummonsReplyWhatToDoScenario() {

    }

    public static class Excusal {
        public static ChainBuilder postExcusalRequest() {
            return postExcusalRequest(Util.getNewScenarioId());
        }

        public static ChainBuilder postExcusalRequest(String scenarioId) {
            return Util.group(scenarioId + GROUP_NAME + " - POST - Process Excusal Request")
                .on(exec(
                        http("POST - Summons Reply - Process Excusal Request")
                            .post(BASE_URL + "/process")
                            .headers(Util.COMMON_HEADERS)
                            .formParam("processActionType", "excusal")
                            .formParam("_csrf", "#{csrf}")
                            .check(Util.validatePageIdentifier("process - what to do"))
                            .check(Util.validateHeading("Grant or refuse an excusal"))
                    )
                );
        }

        public static ChainBuilder postExcusalGrant() {
            return postExcusalGrant(Util.getNewScenarioId());
        }

        public static ChainBuilder postExcusalGrant(String scenarioId) {
            return Util.group(scenarioId + GROUP_NAME + " - POST - Excusal Request - GRANT")
                .on(feed(Feeders.EXCUSAL_CODE_FEEDER)
                    .exec(
                        http("POST - Summons Reply - Grant Excusal Request")
                            .post(BASE_URL + "/excusal")
                            .headers(Util.COMMON_HEADERS)
                            .formParam("excusalCode", "#{exc_code}")
                            .formParam("excusalDecision", "GRANT")
                            .formParam("_csrf", "#{csrf}")
                            .checkIf(session -> Objects.equals(session.getString("owner"), "400")).then(
                                Util.validatePageIdentifier("Your work - To do"),
                                substring("Excusal granted")
                            ).checkIf(session -> !Objects.equals(session.getString("owner"), "400")).then(
                                Util.validatePageIdentifier("Homepage")
                            )
                    )
                );
        }

        public static ChainBuilder postExcusalRefuse() {
            return postExcusalRefuse(Util.getNewScenarioId());
        }

        public static ChainBuilder postExcusalRefuse(String scenarioId) {
            return Util.group(scenarioId + GROUP_NAME + " - POST - Excusal Request - REFUSE")
                .on(feed(Feeders.EXCUSAL_CODE_FEEDER)
                    .exec(
                        http("POST - Summons Reply - Refuse Excusal Request")
                            .post(BASE_URL + "/excusal")
                            .headers(Util.COMMON_HEADERS)
                            .formParam("excusalCode", "#{exc_code}")
                            .formParam("excusalDecision", "REFUSE")
                            .formParam("_csrf", "#{csrf}")
                            .checkIf(session -> Objects.equals(session.getString("owner"), "400")).then(
                                Util.validatePageIdentifier("Your work - To do"),
                                substring("Excusal refused")
                            ).checkIf(session -> !Objects.equals(session.getString("owner"), "400")).then(
                                Util.validatePageIdentifier("Homepage")
                            )
                    )
                );
        }
    }

    public static class Deferral {

        public static ChainBuilder postDeferralRequest() {
            return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - POST - Process Deferral Request")
                .on(exec(
                        http("POST - Summons Reply - Process Deferral Request")
                            .post(BASE_URL + "/process")
                            .headers(Util.COMMON_HEADERS)
                            .formParam("processActionType", "deferral")
                            .formParam("_csrf", "#{csrf}")
                            .check(Util.validatePageIdentifier("process - what to do"))
                            .check(Util.validateHeading("Enter the juror's preferred start dates"))
                    ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
                );
        }

        public static ChainBuilder getPostDeferralRequest(int dateCount) {
            return getPostDeferralRequest(Util.getNewScenarioId(), dateCount);
        }

        public static ChainBuilder getPostDeferralRequest(String scenarioId, int dateCount) {
            return Util.group(scenarioId + GROUP_NAME + " - POST - Summons Reply - Deferral Request - Dates -"
                + " " + dateCount)
                .on(
                    exec(
                        http("POST - Summons Reply - Deferral Request - Dates - " + dateCount)
                            .post(BASE_URL + "/deferral-dates-post")
                            .headers(Util.COMMON_HEADERS)
                            .formParam("deferredToDate1", dateCount >= 1 ? Util.createDateString(dateFormatter) : "")
                            .formParam("deferredToDate2", dateCount >= 2 ? Util.createDateString(dateFormatter) : "")
                            .formParam("deferredToDate3", dateCount >= 3 ? Util.createDateString(dateFormatter) : "")
                            .formParam("_csrf", "#{csrf}")
                            .check(Util.validatePageIdentifier("process - what to do"))
                            .check(Util.validateHeading("Defer this juror"))
                    ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
                );
        }

        public static ChainBuilder postDeferral() {
            return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - POST - Deferral Request")
                .on(feed(Feeders.DEFERAL_CODE_FEEDER)
                    .exec(
                        http("POST - Summons Reply - Deferral Request")
                            .post(BASE_URL + "/deferral")
                            .headers(Util.COMMON_HEADERS)
                            .formParam("deferralReason", "#{exc_code}")
                            .formParam("deferralOption", Util.createDateStringMonday(deferralDateFormatter))
                            .formParam("deferralDate", "")
                            .formParam("jurorNumber", "#{juror_number}")
                            .formParam("_csrf", "#{csrf}")
                            .formParam("version", "")
                            .checkIf(session -> Objects.equals(session.getString("owner"), "400")).then(
                                Util.validatePageIdentifier("Your work - To do"),
                                substring("Deferral granted")
                            ).checkIf(session -> !Objects.equals(session.getString("owner"), "400")).then(
                                Util.validatePageIdentifier("Homepage")
                            )
                    ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
                );
        }
    }
}
