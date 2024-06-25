package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class ReportScenario {
    private static final String GROUP_NAME = "Reports";

    public static final String BASE_URL_REPORTS = "/reports";

    public static final String BASE_URL_STATISTICS = "/statistics";

    public static ChainBuilder viewAllReports() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - GET - View Reports")
            .on(
                exec(
                    http("GET - Reports")
                        .get(BASE_URL_REPORTS)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("Reports"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder viewStatistics() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - GET - View Reports - Statistics")
            .on(
                exec(
                    http("GET - Reports - Statistics")
                        .get(BASE_URL_STATISTICS)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("Statistics"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder viewDailyUtilisation() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - GET - View Reports - Statistics - Daily "
                + "Utilisation")
            .on(
                exec(
                    http("GET - Reports - Statistics - Daily Utilisation")
                        .get("/reporting/daily-utilisation")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.saveCsrf())
                        .check(Util.validatePageIdentifier(
                            "Reports - Daily wastage and utilisation report - select dates"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder postDailyUtilisation(LocalDate from, LocalDate to) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - POST - View Reports - Statistics - Daily "
                + "Utilisation")
            .on(
                exec(
                    http("POST - Reports - Statistics - Daily Utilisation")
                        .post("/reporting/daily-utilisation/report")
                        .headers(Util.COMMON_HEADERS)
                        .formParam("dateFrom", dateFormatter.format(from))
                        .formParam("dateTo", dateFormatter.format(to))
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier(
                            "Reports - Daily wastage and utilisation report - Uncompleted jurors warning"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder viewDailyUtilisation(LocalDate from, LocalDate to) {
        return exec(
            viewDailyUtilisation(),
            postDailyUtilisation(from, to),
            viewDailyUtilisationReport(from, to)
        );
    }

    public static ChainBuilder prepareMonthlyUtilisation() {
        return exec(
            viewMonthlyUtilisationPrepare(),
            postMonthlyUtilisationPrepare()
        );
    }

    private static ChainBuilder postMonthlyUtilisationPrepare() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - POST - View Reports - Statistics - Monthly "
                + "Utilisation - Prepare")
            .on(
                exec(
                    http("POST - Reports - Statistics - Monthly Utilisation - Prepare")
                        .post("/reporting/monthly-utilisation/prepare")
                        .headers(Util.COMMON_HEADERS)
                        .formParam("selectMonth", Util.getMonthStart())
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier(
                            "Reports - Prepare monthly utilisation report - Confirm preparation"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
                    .exec(
                        http("GET - Reports - Statistics - Monthly Utilisation - Prepare - Final")
                            .get("/reporting/prepare-monthly-utilisation/report/" + Util.getMonthStart())
                            .headers(Util.COMMON_HEADERS)
                            .check(Util.validatePageIdentifier(
                                "Reports - Monthly wastage and utilisation report"))
                    ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    private static ChainBuilder viewMonthlyUtilisationPrepare() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - GET - View Reports - Statistics - Monthly "
                + "Utilisation - Prepare")
            .on(
                exec(
                    http("GET - Reports - Statistics - Prepare Monthly Utilisation")
                        .get("/reporting/monthly-utilisation/prepare")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.saveCsrf())
                        .check(Util.validatePageIdentifier(
                            "Reports - Prepare monthly utilisation report - Select month"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    private static ChainBuilder viewDailyUtilisationReport(LocalDate from, LocalDate to) {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - GET - View Reports - Statistics - Daily "
                + "Utilisation - Data")
            .on(
                exec(
                    http("GET - Reports - Statistics - Daily Utilisation - Data")
                        .get("/reporting/daily-utilisation/report/dateRange")
                        .headers(Util.COMMON_HEADERS)
                        .queryParam("fromDate", DateTimeFormatter.ISO_DATE.format(from))
                        .queryParam("toDate", DateTimeFormatter.ISO_DATE.format(to))
                        .check(Util.validatePageIdentifier("Reports - Daily wastage and utilisation report"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

}
