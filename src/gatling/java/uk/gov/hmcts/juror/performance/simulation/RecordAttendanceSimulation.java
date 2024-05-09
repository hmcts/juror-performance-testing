package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.gen.FeederGenerator;
import uk.gov.hmcts.juror.performance.scenario.JurorManagementScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.NavBarScenario;

import java.time.Duration;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.forever;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class RecordAttendanceSimulation {
    public static ScenarioBuilder getScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Record attendance - Check-in / Bulk check out")
            .group("Record attendance - Check-in / Bulk check out").on(
                forever().on(
                    pace(Duration.ofSeconds(secondsPerTransaction))
                        .feed(Feeders.OWNER_FEEDER_COURT)
                        .exec(LoginScenario.login())
                        .exec(session -> {
                            String owner = session.getString("owner");
                            FeederGenerator feederGenerator =
                                Feeders.getOrCreateByOwnerAndStatus(owner, "2");
                            return session.set("juror_number_gen", feederGenerator);
                        })
                        .exec(NavBarScenario.getJurorManagement())
                        .exec(JurorManagementScenario.getRecordAttendance())
                        .repeat(12).on(
                            exec(session -> {
                                FeederGenerator gen = Objects.requireNonNull(session.get("juror_number_gen"));
                                return session
                                    .set("juror_number", gen.generate().get("juror_number"));
                            }).exec(
                                JurorManagementScenario.checkInJuror()
                            )
                        )
                        .exec(JurorManagementScenario.checkOutAllJurors())
                ));
    }
}
