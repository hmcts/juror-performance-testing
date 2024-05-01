package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class JurorManagementScenario {
    private static final String GROUP_NAME = "Juror Management";


    public static ChainBuilder getManageJurors() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Manage Jurors")
            .on(exec(
                    http("GET - Juror Management - Manage Jurors")
                        .get("/juror-management/manage-jurors/pools")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("juror-management - manage jurors"))
                        .check(status().is(200))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder getApproveJuror() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Approve Jurors")
            .on(exec(
                    http("GET - Juror Management - Approve Jurors")
                        .get("/juror-management/manage-jurors/approve")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("juror-management - approve jurors"))
                        .check(status().is(200))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder getRecordAttendance() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Record Attendance")
            .on(exec(
                    http("GET - Juror Management - Record Attendance")
                        .get("/juror-management/attendance")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.saveCsrf())
                        .check(Util.validatePageIdentifier("juror-management - attendance"))
                        .check(status().is(200))
                        .check(Util.saveCsrf())
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder checkInJuror() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - POST - Record Attendance - Check in")
            .on(exec(
                    http("POST - Juror Management - Record Attendance - Check in")
                        .post("/juror-management/attendance/check-in")
                        .headers(Util.COMMON_HEADERS)
                        .formParam("jurorNumber", "#{juror_number}")
                        .formParam("time", "08:30am")
                        .formParam("_csrf", "#{csrf}")
                        .check(status().is(200))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder checkOutAllJurors() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - POST - Record Attendance - Check out all jurors")
            .on(exec(
                    http("POST - Juror Management - Record Attendance - Check out all jurors")
                        .post("/juror-management/attendance/check-out-all-jurors")
                        .headers(Util.COMMON_HEADERS)
                        .formParam("checkOutTimeHour", "5")
                        .formParam("checkOutTimeMinute", "30")
                        .formParam("checkOutTimePeriod", "pm")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.saveCsrf())
                        .check(Util.validatePageIdentifier("juror-management - attendance"))
                        .check(status().is(200))
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }


}
