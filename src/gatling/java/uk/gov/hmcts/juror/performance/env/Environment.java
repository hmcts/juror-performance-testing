package uk.gov.hmcts.juror.performance.env;

import io.gatling.javaapi.core.ClosedInjectionStep;
import uk.gov.hmcts.juror.performance.simulation.Simulations;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface Environment {
    Collection<Simulations> getSimulationsToRun();

    Duration getMaxDuration();

    default List<ClosedInjectionStep> simulationProfileClosed(int users, Duration delayStart) {
        return simulationProfileClosed(users, delayStart, 1);
    }

    List<ClosedInjectionStep> simulationProfileClosed(int users, Duration delayStart, long rampMultiplier);

    int getPaceInSeconds(int count, TimeUnit timeUnit);
}
