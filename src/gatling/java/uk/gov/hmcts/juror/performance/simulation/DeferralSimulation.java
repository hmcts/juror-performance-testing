package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordDeferralScenario;
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

public class DeferralSimulation extends AbstractJurorSimulation {
    @Override
    protected ScenarioBuilder getScenario() {
        ChainBuilder responded = group("Juror Record - RESPONDED").on(
            feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("2")).exec(
                LoginScenario.login(),
                JurorRecordSearchScenario.jurorRecordSearch(),
                JurorRecordScenario.getUpdateRecord(),
                JurorRecordUpdateScenario.postUpdateRecordDeferral(),
                randomSwitchOrElse().on(
                    Choice.withWeight(87,
                        JurorRecordDeferralScenario.postDeferalGrant(Util.getNewScenarioId())),
                    Choice.withWeight(13,
                        JurorRecordDeferralScenario.postDeferalRefuse(Util.getLastScenarioId()))
                ).orElse(exitHere())
            )
        );
        ChainBuilder summoned = group("Juror Record - SUMMONED").on(
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
        );

        return scenario("Juror Record update - deferral")
            .exitBlockOnFail().on(exec(
                randomSwitchOrElse().on(
                    Choice.withWeight(50, responded),
                    Choice.withWeight(50, summoned)
                ).orElse(exitHere())
            ));
    }
}
