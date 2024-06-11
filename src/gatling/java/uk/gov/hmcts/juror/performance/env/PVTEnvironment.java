package uk.gov.hmcts.juror.performance.env;

import io.gatling.javaapi.core.ClosedInjectionStep;
import lombok.Getter;
import uk.gov.hmcts.juror.performance.simulation.Simulations;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.rampConcurrentUsers;

@Getter
public class PVTEnvironment implements Environment {
    private final int totalTestTimeSeconds = 300;
    private final int rampTimeSeconds = 300;
    private final List<Simulations> simulationsToRun = List.of(
            Simulations.UTILIZATION_MONTHLY
        //        Simulations.values()
        );

    @Override
    public Duration getMaxDuration() {
        return Duration.ofSeconds(totalTestTimeSeconds + (rampTimeSeconds));
    }

    @Override
    public List<ClosedInjectionStep> simulationProfileClosed(int users, Duration delayStart, long rampMultiplier) {
        return List.of(
            constantConcurrentUsers(0).during(delayStart),
            rampConcurrentUsers(0).to(users)
                .during(Duration.ofSeconds(rampTimeSeconds * rampMultiplier)),
            constantConcurrentUsers(users).during(Duration.ofSeconds(totalTestTimeSeconds)),
            rampConcurrentUsers(users).to(0)
                .during(Duration.ofSeconds(rampTimeSeconds))
        );
    }

    @Override
    public int getPaceInSeconds(int count, TimeUnit timeUnit) {
        return (int) timeUnit.toSeconds(1) / count;
    }
}
