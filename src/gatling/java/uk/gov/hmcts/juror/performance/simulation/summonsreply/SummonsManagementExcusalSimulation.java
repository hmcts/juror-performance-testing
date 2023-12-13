package uk.gov.hmcts.juror.performance.simulation.summonsreply;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordSummonsScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyWhatToDoScenario;
import uk.gov.hmcts.juror.performance.simulation.AbstractJurorSimulation;

import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.scenario;

//https://centralgovernmentcgi.atlassian.net/browse/JM-5312
public class SummonsManagementExcusalSimulation extends AbstractJurorSimulation {
    @Override
    protected ScenarioBuilder getScenario() {
        return scenario("Juror Record update - deferral")
            .exec(
                LoginScenario.LOGIN_AS_BUREAU,
//TODO fix                Feeders.PAPER_DIGITAL_REPLY_TYPE_JUROR_NUBER_FEEDER,
                JurorRecordSearchScenario.JUROR_RECORD_SEARCH,
                JurorRecordScenario.GET_SUMMONS,
                JurorRecordSummonsScenario.GET_SUMMONS_REPLY,
                SummonsReplyScenario.GET_PROCESS_REPLY,
                SummonsReplyWhatToDoScenario.Excusal.POST_EXCUSAL_REQUEST,
                randomSwitch().on(
                    Choice.withWeight(87, SummonsReplyWhatToDoScenario.Excusal.POST_EXCUSAL_GRANT),
                    Choice.withWeight(13, SummonsReplyWhatToDoScenario.Excusal.POST_EXCUSAL_REFUSE)
                )
            );
        //TODO court flow
    }
}
