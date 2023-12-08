package uk.gov.hmcts.juror.performance.scenario.summonsreply;

import io.gatling.javaapi.core.ChainBuilder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.support.generation.generators.value.LocalDateGeneratorImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;

@Slf4j
public class SummonsReplyWhatToDoScenario {
    private static final LocalDateGeneratorImpl LOCAL_DATE_GENERATOR = new LocalDateGeneratorImpl(
        LocalDate.now().plusDays(30),
        LocalDate.now().plusDays(180)
    );
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter deferralDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String GROUP_NAME = "Summons Reply - What To Do";
    public static final String BASE_URL = SummonsReplyPaperScenario.BASE_URL;


    public static class Deferral {

        public static ChainBuilder POST_DEFERRAL_REQUEST = group(GROUP_NAME)
            .on(exec(
                    http("POST - Summons Reply - Deferral Request")
                        .post(BASE_URL + "/process")
                        .headers(Util.COMMON_HEADERS)
                        .formParam("processActionType", "deferral")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("process - what to do"))
                        .check(Util.validateHeading("Enter the juror's preferred start dates"))
                ).exitHereIfFailed()
            );


        private static ChainBuilder getPostDeferralRequest(boolean includeDate1, boolean includeDate2,
                                                           boolean includeDate3) {
            return group(GROUP_NAME)
                .on(exec(
                        http("POST - Summons Reply - Deferral Request - Dates")
                            .post(BASE_URL + "/deferral-dates-post")
                            .headers(Util.COMMON_HEADERS)
                            .formParam("deferredToDate1", includeDate1 ? createDateString(dateFormatter) : "")
                            .formParam("deferredToDate2", includeDate2 ? createDateString(dateFormatter) : "")
                            .formParam("deferredToDate3", includeDate3 ? createDateString(dateFormatter) : "")
                            .formParam("_csrf", "#{csrf}")
                            .check(Util.validatePageIdentifier("process - what to do"))
                            .check(Util.validateHeading("Defer this juror"))
                    ).exitHereIfFailed()
                );
        }

        private static String createDateString(DateTimeFormatter dateTimeFormatter) {
            return LOCAL_DATE_GENERATOR.generateValue().format(dateTimeFormatter);
        }

        public static ChainBuilder POST_DEFERRAL_DATES_1 = getPostDeferralRequest(true, false, false);
        public static ChainBuilder POST_DEFERRAL_DATES_2 = getPostDeferralRequest(true, true, false);
        public static ChainBuilder POST_DEFERRAL_DATES_3 = getPostDeferralRequest(true, true, true);


        public static ChainBuilder POST_DEFERRAL = group(GROUP_NAME)
            .on(feed(Util.DEFERAL_CODE_FEEDER)
                .exec(
                    http("POST - Summons Reply - Deferral Request")
                        .post(BASE_URL + "/deferral")
                        .headers(Util.COMMON_HEADERS)
                        .formParam("deferralReason", "#{exc_code}")
                        .formParam("deferralOption", createDateString(deferralDateFormatter))
                        .formParam("deferralDate", "")
                        .formParam("jurorNumber", "#{juror_number}")
                        .formParam("_csrf", "#{csrf}")
                        .formParam("version", "")

                        .check(Util.validatePageIdentifier("your work - to do"))
                        .check(substring("Deferral granted")))
            ).exitHereIfFailed();
    }
}
