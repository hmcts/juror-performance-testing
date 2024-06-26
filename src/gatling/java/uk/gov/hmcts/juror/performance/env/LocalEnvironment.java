package uk.gov.hmcts.juror.performance.env;

import io.gatling.javaapi.core.ClosedInjectionStep;
import lombok.Getter;
import uk.gov.hmcts.juror.performance.simulation.Simulations;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;

@Getter
public class LocalEnvironment implements Environment {
    private final int totalTestTimeSeconds = 120;
    private final int userOverride = 20;
    private final int paceOverride = 3;
    private final List<Simulations> simulationsToRun = List.of(
        Simulations.DEFERRALS
    );

    @Override
    public Duration getMaxDuration() {
        return Duration.ofSeconds(totalTestTimeSeconds);
    }

    @Override
    public List<ClosedInjectionStep> simulationProfileClosed(int users, Duration delayStart, long rampMultiplier) {
        return List.of(
            constantConcurrentUsers(userOverride).during(Duration.ofSeconds(totalTestTimeSeconds))
        );
    }

    @Override
    public int getPaceInSeconds(int count, TimeUnit timeUnit) {
        return paceOverride;
    }
}
