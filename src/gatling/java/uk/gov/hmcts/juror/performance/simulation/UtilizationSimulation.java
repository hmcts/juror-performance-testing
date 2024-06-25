package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.ReportScenario;

import java.time.Duration;
import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class UtilizationSimulation {

    public static ScenarioBuilder getDailyScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Utilization Report - Daily")
            .group("Utilization Report - Daily").on(
                feed(Feeders.OWNER_FEEDER_COURT)
                    .exec(LoginScenario.login())
                    .forever().on(
                        pace(Duration.ofSeconds(secondsPerTransaction))
                            .exec(
                                ReportScenario.viewAllReports(),
                                ReportScenario.viewStatistics(),
                                ReportScenario.viewDailyUtilisation(
                                    LocalDate.now().minusDays(7),
                                    LocalDate.now())
                            )
                    )
            );
    }

    public static ScenarioBuilder getMonthlyScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Utilization Report - Monthly")
            .group("Utilization Report - Monthly").on(
                feed(Feeders.OWNER_FEEDER_COURT)
                    .exec(LoginScenario.login())
                    .forever().on(
                        pace(Duration.ofSeconds(secondsPerTransaction))
                            .exec(
                                ReportScenario.viewAllReports(),
                                ReportScenario.viewStatistics(),
                                ReportScenario.prepareMonthlyUtilisation()
                            )
                    )
            );
    }
}
