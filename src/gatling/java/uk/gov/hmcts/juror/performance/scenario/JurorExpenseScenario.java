package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class JurorExpenseScenario {
    private static final String GROUP_NAME = "Juror Expenses";

    public static final String BASE_URL =
        "/juror-management/unpaid-attendance/expense-record/#{juror_number}/#{loc_code}";
    public static final String BASE_URL_EXPENSES =
        "/juror-management/expenses/#{juror_number}/#{loc_code}";


    public static ChainBuilder viewTotalInDraftExpenses() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Expenses - Draft")
            .on(
                exec(
                    http("GET - Juror Record - Expenses - draft")
                        .get(BASE_URL + "/draft")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("juror-management - unpaid attendance - expense record"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder getAddDraftExpense() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Expenses - Draft - day")
            .on(
                exec(
                    http("GET - Juror Record - Expenses - draft - day")
                        .get(BASE_URL_EXPENSES + "/enter-expenses")
                        .queryParam("date", "#{attendance_date}")
                        .queryParam("page", "1")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.saveCsrf())
                        .check(Util.validatePageIdentifier("juror-management - unpaid attendance - daily expenses"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder postAddDraftExpenseSaveAndBack() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - POST - Expenses - Draft - day")
            .on(
                exec(
                    http("POST - Juror Record - Expenses - draft - day")
                        .post(BASE_URL_EXPENSES + "/enter-expenses")
                        .queryParam("date", "#{attendance_date}")
                        .queryParam("page", "1")
                        .queryParam("action", "back")

                        .formParam("totalTravelTime-hour", "00")
                        .formParam("totalTravelTime-minute", "00")
                        .formParam("payAttendance", "FULL_DAY")
                        .formParam("travelType", "car")
                        .formParam("carPassengers", "1")
                        .formParam("motoPassengers", "")
                        .formParam("milesTravelled", "15")
                        .formParam("parking", "2.00")
                        .formParam("publicTransport", "0.00")
                        .formParam("taxi", "0.00")
                        .formParam("foodAndDrink", "MORE_THAN_10_HOURS")
                        .formParam("smartcardSpend", "")
                        .formParam("lossOfEarnings", "4")
                        .formParam("extraCareCosts", "3")
                        .formParam("otherCosts", "2")
                        .formParam("otherCostsDescription", "")
                        .formParam("paymentMethod", "BACS")
                        .formParam("jurorNumber", "#{juror_number}")
                        .formParam("locCode", "#{loc_code}")
                        .formParam("expenseStatus", "draft")
                        .formParam("total", "")
                        .formParam("_csrf", "#{csrf}")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("juror-management - unpaid attendance - expense record"))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }
}
