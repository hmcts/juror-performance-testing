package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.YourWorkScenario;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class YourWorkSimulation {

    public static ScenarioBuilder getScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Your Work")
            .feed(Feeders.OWNER_FEEDER_BUREAU)
            .group("Your Work").on(
                exec(LoginScenario.loginAsBureau())
                    .forever().on(
                        pace(Duration.ofSeconds(secondsPerTransaction))
                            .exec(
                                YourWorkScenario.getToDo(),
                                YourWorkScenario.getAwaitingInformation(),
                                YourWorkScenario.getCompleted()
                            )
                    )
            );
    }
}
