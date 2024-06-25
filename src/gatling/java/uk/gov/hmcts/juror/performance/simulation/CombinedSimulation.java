package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.PopulationBuilder;
import uk.gov.hmcts.juror.performance.Config;

public class CombinedSimulation extends BaseSimulation {

    public CombinedSimulation() {
        super();
        setup();
    }

    public void setup() {
        addAssertions(
            setUp(Config.ENV.getSimulationsToRun()
                .stream()
                .map(Simulations::getPopulationBuilder)
                .toList().toArray(new PopulationBuilder[0]))
                .maxDuration(Config.ENV.getMaxDuration())
        );
    }
}
