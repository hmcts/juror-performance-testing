package uk.gov.hmcts.juror.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CheckBuilder;
import io.gatling.javaapi.core.Session;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.support.generation.generators.value.LocalDateGeneratorImpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.doIfEqualsOrElse;
import static io.gatling.javaapi.core.CoreDsl.exec;

@Slf4j
public class Util {

    public static final long DEFAULT_THINK_TIME_MS = 200;
    private static final AtomicInteger COUNTER;
    public static final Map<String, String> COMMON_HEADERS;
    public static final DateTimeFormatter STANDARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final LocalDateGeneratorImpl LOCAL_DATE_GENERATOR = new LocalDateGeneratorImpl(
        LocalDate.now().plusWeeks(10),//Must be after jurorPool ret_date (check data generation library)
        LocalDate.now().plusWeeks(25)
    );

    static {
        COUNTER = new AtomicInteger(0);
        COMMON_HEADERS = Map.of(
            "accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,"
                + "application/signed-exchange;v=b3;q=0.9",
            "accept-encoding", "gzip, deflate, br",
            "accept-language", "en-GB,en;q=0.9",
            "sec-fetch-dest", "document",
            "sec-fetch-mode", "navigate",
            "sec-fetch-site", "same-origin",
            "sec-fetch-user", "?1",
            "upgrade-insecure-requests", "1",
            "origin", "null"
        );
    }

    public static CheckBuilder.Final saveCsrf() {
        return css("input[name='_csrf']", "value").saveAs("csrf");
    }

    public static CheckBuilder saveBody() {
        return bodyString().saveAs("body");
    }

    public static Session printBody(Session session) {
        log.info("Body: " + session.getString("body"));
        return session;
    }


    public static CheckBuilder.Final validatePageIdentifier(String pageIdentifier) {
        return css("meta[name='pageIdentifier']", "content").is(pageIdentifier);
    }

    public static CheckBuilder.Final validateHeading(String headingValue) {
        return css("h1.govuk-heading-l").is(headingValue);
    }


    public static ChainBuilder isBureau(ChainBuilder bureauActions,
                                        ChainBuilder courtActions) {
        return doIfEqualsOrElse("#{owner}", "400")
            .then(bureauActions).orElse(courtActions);
    }

    public static String createDateString(DateTimeFormatter dateTimeFormatter) {
        return LOCAL_DATE_GENERATOR.generateValue().format(dateTimeFormatter);
    }

    public static String createDateStringMonday(DateTimeFormatter dateTimeFormatter) {
        LocalDate date = LOCAL_DATE_GENERATOR.generateValue();
        if (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.with(DayOfWeek.MONDAY);
        }
        return date.format(dateTimeFormatter);
    }

    static int count = 0;

    public static ChainBuilder printSessionVariables() {
        return exec(session -> {
            if (!Config.DEBUG) {
                return session;
            }
            try {
                count++;
                log.info(count + " Session variables: " + session);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return session;
        });
    }

    public static String getNewScenarioId() {
        return formatNumber(COUNTER.incrementAndGet());
    }

    public static String getLastScenarioId() {
        return formatNumber(COUNTER.get());
    }

    private static String formatNumber(int value) {
        return String.format("%02d: ", value);
    }

    public static void resetCounter() {
        COUNTER.set(0);
    }

    public static String convertStringDate(String postponeNewServiceStart, String fromPattern, String toPattern) {
        return LocalDate.parse(postponeNewServiceStart,
                DateTimeFormatter.ofPattern(fromPattern))
            .format(DateTimeFormatter.ofPattern(toPattern));
    }


}
