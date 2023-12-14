package uk.gov.hmcts.juror.performance.simulation;


import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;

import static io.gatling.javaapi.core.CoreDsl.scenario;

//https://centralgovernmentcgi.atlassian.net/browse/JM-5309
public class JurorRecordSearchSimulation extends AbstractJurorSimulation {

    @Override
    protected ScenarioBuilder getScenario() {
        return scenario("Juror Record Search")
            .exec(LoginScenario.loginAsCourt(),
                Feeders.JUROR_NUMBER_MATCHING_OWNER_FEEDER,
                JurorRecordSearchScenario.jurorRecordSearch()
            );
    }
}
