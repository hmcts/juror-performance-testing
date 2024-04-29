package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.gen.FeederGenerator;
import uk.gov.hmcts.juror.performance.scenario.AvailablePoolsScenario;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordPostponeScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordUpdateScenario;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.gatling.javaapi.core.CoreDsl.doIfOrElse;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static uk.gov.hmcts.juror.performance.Feeders.jdbcFeeder;

public class PostponeSimulation extends AbstractJurorSimulation {
    public static ScenarioBuilder getScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Juror Record update - postpone")
            .group("Juror Record update - postpone").on(
                feed(Feeders.OWNER_FEEDER_COURT)
                    .exec(LoginScenario.login())
                    .exec(session -> {
                        String owner = session.getString("owner");
                        FeederGenerator feederGenerator = Feeders.getOrCreateByOwnerAndStatus(owner, "2");
                        return session.set("juror_number_gen", feederGenerator);
                    })
                    .forever().on(
                        pace(Duration.ofSeconds(secondsPerTransaction))
                            .exec(session -> {
                                FeederGenerator gen = Objects.requireNonNull(session.get("juror_number_gen"));
                                Map<String, Object> data = gen.generate();
                                return session.set("juror_number", data.get("juror_number"));
                            })
                            .exec(
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
                    ));

    }

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
