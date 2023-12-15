package uk.gov.hmcts.juror.performance.simulation.summonsreply;

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
import static io.gatling.javaapi.core.CoreDsl.exitHere;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.randomSwitchOrElse;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class SummonsManagementDeferralSimulation extends AbstractJurorSimulation {
    @Override
    protected ScenarioBuilder getScenario() {
        return scenario("Juror Record update - deferral")
            .exitBlockOnFail(exec(
                randomSwitchOrElse().on(
                    //Juror Record - RESPONDED
                    Choice.withWeight(50,
                        group("Juror Record - RESPONDED").on(
                            feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("2")).exec(
                                LoginScenario.login(),
                                JurorRecordSearchScenario.jurorRecordSearch(),
                                JurorRecordScenario.getUpdateRecord(),
                                JurorRecordUpdateScenario.postUpdateRecordDeferral(),
                                randomSwitchOrElse().on(
                                    Choice.withWeight(87,
                                        JurorRecordUpdateScenario.Deferral.postDeferalGrant(Util.getNewScenarioId())),
                                    Choice.withWeight(13,
                                        JurorRecordUpdateScenario.Deferral.postDeferalRefuse(Util.getLastScenarioId()))
                                ).orElse(exitHere())
                            )
                        )
                    ),
                    //Summons Management - SUMMONED
                    Choice.withWeight(50,
                        group("Juror Record - SUMMONED").on(
                            feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("1")).exec(
                                LoginScenario.login(),
                                JurorRecordSearchScenario.jurorRecordSearch(),
                                JurorRecordScenario.getSummons(),
                                JurorRecordSummonsScenario.getSummonsReply(),
                                SummonsReplyScenario.getProcessReply(),
                                SummonsReplyWhatToDoScenario.Deferral.postDeferralRequest(),
                                //Select how many dates to enter
                                randomSwitchOrElse().on(
                                    Choice.withWeight(10,
                                        SummonsReplyWhatToDoScenario.Deferral.getPostDeferralRequest(
                                            Util.getNewScenarioId(), 1)),
                                    Choice.withWeight(20,
                                        SummonsReplyWhatToDoScenario.Deferral.getPostDeferralRequest(
                                            Util.getLastScenarioId(), 2)),
                                    Choice.withWeight(70,
                                        SummonsReplyWhatToDoScenario.Deferral.getPostDeferralRequest(
                                            Util.getLastScenarioId(), 3))
                                ).orElse(exitHere()),
                                SummonsReplyWhatToDoScenario.Deferral.postDeferral()
                            )
                        )
                    )
                ).orElse(exitHere())
            ));
    }
}
