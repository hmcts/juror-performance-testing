package uk.gov.hmcts.juror.performance.simulation.summonsreply;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordSummonsScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordUpdateScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyWhatToDoScenario;
import uk.gov.hmcts.juror.performance.simulation.AbstractJurorSimulation;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.scenario;

//https://centralgovernmentcgi.atlassian.net/browse/JM-5312
public class SummonsManagementDeferralSimulation extends AbstractJurorSimulation {
    @Override
    protected ScenarioBuilder getScenario() {
        System.out.println("Feeder keys: " + Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.keySet());
        System.out.println("Val: " + Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("2"));
        return scenario("Juror Record update - deferral")
            .exec(
                Util.printSessionVariables(),
                randomSwitch().on(
                    //Juror Record - RESPONDED
                    Choice.withWeight(50,
                        feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("2")).exec(
                            Util.printSessionVariables(),
                            getStandardChainBuilder()
                        )
                    ),
                    //Summons Management - SUMMONED
                    Choice.withWeight(50, feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("1"))
                        .exec(Util.printSessionVariables(),
                            getStandardChainBuilder())
                    )
                )
            );
    }

    private ChainBuilder getStandardChainBuilder() {
        return exec(LoginScenario.LOGIN,
            JurorRecordSearchScenario.JUROR_RECORD_SEARCH,
            Util.isBureau(
                exec(
                    JurorRecordScenario.GET_SUMMONS,
                    JurorRecordSummonsScenario.GET_SUMMONS_REPLY,
                    Util.printSessionVariables(),
                    SummonsReplyScenario.GET_PROCESS_REPLY,
                    SummonsReplyWhatToDoScenario.Deferral.POST_DEFERRAL_REQUEST,
                    //Select how many dates to enter
                    randomSwitch().on(
                        Choice.withWeight(10, SummonsReplyWhatToDoScenario.Deferral.POST_DEFERRAL_DATES_1),
                        Choice.withWeight(20, SummonsReplyWhatToDoScenario.Deferral.POST_DEFERRAL_DATES_2),
                        Choice.withWeight(70, SummonsReplyWhatToDoScenario.Deferral.POST_DEFERRAL_DATES_3)
                    ),
                    SummonsReplyWhatToDoScenario.Deferral.POST_DEFERRAL
                ),
                exec(
                    JurorRecordScenario.GET_UPDATE_RECORD,
                    JurorRecordUpdateScenario.POST_UPDATE_RECORD_DEFERRAL,
                    randomSwitch().on(
                        Choice.withWeight(66, JurorRecordUpdateScenario.Deferral.POST_DEFERAL_GRANT),
                        Choice.withWeight(34, JurorRecordUpdateScenario.Deferral.POST_DEFERAL_REFUSE)
                    )
                )
            ));
    }
}
