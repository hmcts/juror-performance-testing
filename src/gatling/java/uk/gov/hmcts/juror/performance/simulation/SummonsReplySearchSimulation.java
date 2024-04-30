package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplySearchScenario;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.uniformRandomSwitch;

public class SummonsReplySearchSimulation {

    public static ScenarioBuilder getScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Summons Reply Search")
            .group("Summons Reply Search").on(
                exec(session -> session.set("owner", "400"))
                    .exec(LoginScenario.login())
                    .forever().on(
                        pace(Duration.ofSeconds(secondsPerTransaction))
                            .feed(Feeders.JUROR_NUMBER_RESPONSE_FEEDER)
                            .exec(
                                SummonsReplySearchScenario.getSearch(),
                                uniformRandomSwitch().on(
                                    SummonsReplySearchScenario.postSearchJurorNumber(),
                                    SummonsReplySearchScenario.postSearchLastName(),
                                    SummonsReplySearchScenario.postSearchPoolNumber()
                                )
                            )
                    )
            );
    }
}
