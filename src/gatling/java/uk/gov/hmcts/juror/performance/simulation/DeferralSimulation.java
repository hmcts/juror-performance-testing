package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.gen.FeederGenerator;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordDeferralScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordSummonsScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordUpdateScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyScenario;
import uk.gov.hmcts.juror.performance.scenario.summonsreply.SummonsReplyWhatToDoScenario;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.exitHere;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.randomSwitchOrElse;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static uk.gov.hmcts.juror.performance.Feeders.jdbcFeeder;

public class DeferralSimulation extends AbstractJurorSimulation {


    public static ScenarioBuilder getScenarioBuilderStatic(int secondsPerTransaction) {

        ChainBuilder responded = Util.group("Juror Record - RESPONDED").on(
            feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("2")).exec(
                LoginScenario.login(),
                JurorRecordSearchScenario.jurorRecordSearch(),
                JurorRecordScenario.getUpdateRecord(),
                JurorRecordUpdateScenario.postUpdateRecordDeferral(),
                randomSwitchOrElse().on(
                    CoreDsl.percent(87).then(
                        JurorRecordDeferralScenario.postDeferalGrant(Util.getNewScenarioId())),
                    CoreDsl.percent(13).then(
                        JurorRecordDeferralScenario.postDeferalRefuse(Util.getLastScenarioId()))
                ).orElse(exitHere())
            )
        );
        ChainBuilder summoned = Util.group("Juror Record - SUMMONED").on(
            feed(Feeders.JUROR_NUMBER_FEEDER_BY_STATUS_MAP.get("1")).exec(
                LoginScenario.login(),
                JurorRecordSearchScenario.jurorRecordSearch(),
                JurorRecordScenario.getSummons(),
                JurorRecordSummonsScenario.getSummonsReply(),
                SummonsReplyScenario.getProcessReply(),
                SummonsReplyWhatToDoScenario.Deferral.postDeferralRequest(),
                //Select how many dates to enter
                randomSwitchOrElse().on(
                    CoreDsl.percent(10).then(
                        SummonsReplyWhatToDoScenario.Deferral.getPostDeferralRequest(
                            Util.getNewScenarioId(), 1)),
                    CoreDsl.percent(20).then(
                        SummonsReplyWhatToDoScenario.Deferral.getPostDeferralRequest(
                            Util.getLastScenarioId(), 2)),
                    CoreDsl.percent(70).then(
                        SummonsReplyWhatToDoScenario.Deferral.getPostDeferralRequest(
                            Util.getLastScenarioId(), 3))
                ).orElse(exitHere()),
                SummonsReplyWhatToDoScenario.Deferral.postDeferral()
            )
        );

        return scenario("Juror Record update - deferral")
            .exitBlockOnFail().on(exec(
                randomSwitchOrElse().on(
                    CoreDsl.percent(50).then(responded),
                    CoreDsl.percent(50).then(summoned)
                ).orElse(exitHere())
            ));
    }


    public static ScenarioBuilder getCourtScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Juror Record update - deferral (Court)")
            .group("Juror Record update - deferral (Court)").on(
                feed(Feeders.OWNER_FEEDER_COURT)
                    .exec(LoginScenario.login())
                    .exec(session -> {
                        String owner = session.getString("owner");
                        FeederGenerator feederGenerator =
                            Feeders.getOrCreateByOwnerAndStatus(owner, "2");
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
                                JurorRecordUpdateScenario.postUpdateRecordDeferral(),
                                JurorRecordDeferralScenario.postDeferalGrant(Util.getNewScenarioId()))
                    ));
    }


    public static ScenarioBuilder getBureauScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Juror Record update - deferral (Bureau)")
            .group("Juror Record update - deferral (Bureau)").on(
                exec(session -> session.set("juror_number_gen", Feeders.JUROR_NUMBER_REPLY_TYPE_BUREAU_FEEDER).set(
                    "owner",
                    "400")),
                exec(LoginScenario.login()
                    .forever().on(
                        pace(Duration.ofSeconds(secondsPerTransaction))
                            .exec(session -> {
                                FeederGenerator gen = Objects.requireNonNull(session.get("juror_number_gen"));
                                Map<String, Object> data = gen.generate();
                                return session
                                    .set("juror_number", data.get("juror_number"))
                                    .set("reply_type", data.get("reply_type"));
                            })
                            .exec(
                                JurorRecordSearchScenario.jurorRecordSearch(),
                                JurorRecordScenario.getSummons(),
                                JurorRecordSummonsScenario.getSummonsReply(),
                                SummonsReplyScenario.getProcessReply(),
                                SummonsReplyWhatToDoScenario.Deferral.postDeferralRequest(),
                                SummonsReplyWhatToDoScenario.Deferral.getPostDeferralRequest(
                                    Util.getNewScenarioId(), 1),
                                SummonsReplyWhatToDoScenario.Deferral.postDeferral()
                            )
                    )
                )
            );
    }

    @Override
    protected ScenarioBuilder getScenario() {
        return getScenarioBuilderStatic(2);
    }
}
