package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.AvailablePoolsScenario;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordPostponeScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordUpdateScenario;

import static io.gatling.javaapi.core.CoreDsl.doIfOrElse;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PostponeSimulation extends AbstractJurorSimulation {
    @Override
    protected ScenarioBuilder getScenario() {
        ChainBuilder responded = group("Juror Record - RESPONDED").on(
            feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("2")).exec(
                LoginScenario.login(),
                JurorRecordSearchScenario.jurorRecordSearch(),
                JurorRecordScenario.getUpdateRecord(),
                JurorRecordUpdateScenario.postUpdateRecordPostpone(),
                JurorRecordPostponeScenario.postPostponeDate(),
                doIfOrElse(session -> {
                    String headingValue = session.getString("headingValue");
                    return headingValue != null && headingValue.equals("Select a pool for this date");
                }).then(
                    exec(AvailablePoolsScenario.postPoolRequest())
                ).orElse(
                    exec(session -> session.set("deferralDateAndPoolDate",
                        Util.convertStringDate(session.getString("postponeNewServiceStart"), "dd/MM/yyyy",
                            "yyyy-MM-dd"))
                    ).exec(AvailablePoolsScenario.postDeferralMaintenance(Util.getLastScenarioId()))
                )
            )
        );

        return scenario("Juror Record update - postpone")
            .exitBlockOnFail().on(exec(responded));
    }
}
