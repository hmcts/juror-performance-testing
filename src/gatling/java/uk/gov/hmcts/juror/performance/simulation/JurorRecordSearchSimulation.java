package uk.gov.hmcts.juror.performance.simulation;


import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.gen.FeederGenerator;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static uk.gov.hmcts.juror.performance.Feeders.jdbcFeeder;

public class JurorRecordSearchSimulation extends AbstractJurorSimulation {

    public static ScenarioBuilder getScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Juror Record Search")
            .group("Juror Record Search").on(
                feed(Feeders.OWNER_FEEDER)
                    .exec(LoginScenario.login())
                    .exec(session -> {
                        String owner = session.getString("owner");
                        FeederGenerator feederGenerator = new FeederGenerator(
                            jdbcFeeder("select juror_number from juror_mod.juror_pool where owner = '" + owner + "' "
                                + "and juror_number::decimal < 300000000"),
                            "juror_number");
                        return session.set("juror_number_gen", feederGenerator);
                    })
                    .forever().on(
                        pace(Duration.ofSeconds(secondsPerTransaction))
                            .exec(session -> {
                                FeederGenerator gen = Objects.requireNonNull(session.get("juror_number_gen"));
                                Map<String, Object> data = gen.generate();
                                return session.set("juror_number", data.get("juror_number"));
                            })
                            .exec(JurorRecordSearchScenario.jurorRecordSearch())
                    ));
    }

    @Override
    protected ScenarioBuilder getScenario() {
        return getScenarioBuilderStatic(60);
    }
}
