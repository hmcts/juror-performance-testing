package uk.gov.hmcts.juror.performance.simulation.summonsreply;

import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordSummonsScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyPaperScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyWhatToDoScenario;
import uk.gov.hmcts.juror.performance.simulation.AbstractJurorSimulation;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.scenario;

//https://centralgovernmentcgi.atlassian.net/browse/JM-5312
public class SummonsManagementDeferralSimulation extends AbstractJurorSimulation {
    @Override
    protected ScenarioBuilder getScenario() {
        return scenario("Juror Record update - deferral")
            .exec(LoginScenario.LOGIN_AS_BUREAU,
                //TODO change to summons reply search
                feed(
                    Util.jdbcFeeder("select juror_number from juror_mod.juror_pool jp  "
                        + "where owner = '400' and status = 1").circular()
                ).exec(JurorRecordSearchScenario.JUROR_RECORD_SEARCH),
                JurorRecordScenario.GET_SUMMONS,
                //TODO mix digital & paper
                JurorRecordSummonsScenario.GET_SUMMONS_REPLY_PAPER,

                SummonsReplyPaperScenario.GET_PROCESS_REPLY,
                SummonsReplyWhatToDoScenario.Deferral.POST_DEFERRAL_REQUEST,
                SummonsReplyWhatToDoScenario.Deferral.POST_DEFERRAL_DATES_3,
                SummonsReplyWhatToDoScenario.Deferral.POST_DEFERRAL);
    }
}
