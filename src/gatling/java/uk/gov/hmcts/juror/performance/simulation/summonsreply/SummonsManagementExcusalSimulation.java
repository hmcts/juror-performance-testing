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
import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.randomSwitchOrElse;
import static io.gatling.javaapi.core.CoreDsl.scenario;

//https://centralgovernmentcgi.atlassian.net/browse/JM-5312
public class SummonsManagementExcusalSimulation extends AbstractJurorSimulation {

    @Override
    protected ScenarioBuilder getScenario() {
        Util.resetCounter();
        //TODO random number feeder
        return scenario("Juror Record update - Excusal")
            .exitBlockOnFail(exec(
                randomSwitchOrElse().on(
                    //Juror Record - RESPONDED
                    Choice.withWeight(0,//TODO once code is implemented on BE/FE set to 50
                        group("Juror Record - RESPONDED").on(
                            feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("2")).exec(
                                LoginScenario.login(),
                                JurorRecordSearchScenario.jurorRecordSearch(),
                                JurorRecordScenario.getUpdateRecord(),
                                JurorRecordUpdateScenario.postUpdateRecordExcusal(),
                                randomSwitch().on(
                                    Choice.withWeight(66,
                                        JurorRecordUpdateScenario.Excusal.postExcusalGrant(Util.getNewScenarioId())),
                                    Choice.withWeight(34,
                                        JurorRecordUpdateScenario.Excusal.postExcusalRefuse(Util.getLastScenarioId()))
                                )
                            )
                        )
                    ),
                    //Summons Management - SUMMONED
                    Choice.withWeight(100,//TODO once code is implemented on BE/FE set to 50
                        group("Juror Record - SUMMONED").on(
                            feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("1")).exec(
                                LoginScenario.login(),
                                JurorRecordSearchScenario.jurorRecordSearch(),
                                JurorRecordScenario.getSummons(),
                                JurorRecordSummonsScenario.getSummonsReply(),
                                SummonsReplyScenario.getProcessReply(),
                                SummonsReplyWhatToDoScenario.Excusal.postExcusalRequest(),
                                randomSwitchOrElse().on(
                                    Choice.withWeight(87,
                                        SummonsReplyWhatToDoScenario.Excusal.postExcusalGrant(Util.getNewScenarioId())),
                                    Choice.withWeight(13,
                                        SummonsReplyWhatToDoScenario.Excusal.postExcusalRefuse(Util.getLastScenarioId()))
                                ).orElse(exitHere())
                            )
                        )
                    )
                ).orElse(exitHere())
            ));
    }
}
