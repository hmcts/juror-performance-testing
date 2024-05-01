package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.performance.Config;
import uk.gov.hmcts.juror.performance.TestType;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.http.HttpDsl.http;

@Slf4j
public abstract class AbstractJurorSimulation extends BaseSimulation {


    protected AbstractJurorSimulation() {
        setupClosed();
    }

    @Override
    public void before() {
        super.before();
        log.info(Config.asString());
    }

    protected ScenarioBuilder getScenarioBuilder() {
        Util.resetCounter();
        return getScenario();
    }

    //Method kept as useful for manual testing
    public void setupOpen() {
        addAssertions(
            setUp(
                getScenarioBuilder()
                    .injectOpen(simulationProfileOpen().toArray(new OpenInjectionStep[0]))
            )
        );
    }

    public void setupClosed() {
        addAssertions(
            setUp(
                getScenarioBuilder()
                    .injectClosed(simulationProfileClosed().toArray(new ClosedInjectionStep[0]))
            )
        );
    }

    protected abstract ScenarioBuilder getScenario();

    //Method kept as useful for manual testing
    public List<OpenInjectionStep> simulationProfileOpen() {
        return switch (Config.TEST_TYPE) {
            case PERFORMANCE -> {
                if (Config.DEBUG) {
                    yield List.of(atOnceUsers(1));
                } else {
                    yield List.of(
                        rampUsersPerSec(0.00).to(Config.USERS_PER_SECOND)
                            .during(Duration.ofSeconds(Config.RANK_UP_TIME_SECONDS)),
                        constantUsersPerSec(Config.USERS_PER_SECOND).during(
                            Duration.ofSeconds(Config.TEST_DURATION_SECONDS)),
                        rampUsersPerSec(Config.USERS_PER_SECOND).to(0.00)
                            .during(Duration.ofSeconds(Config.RANK_DOWN_TIME_SECONDS))
                    );
                }
            }
            case PIPELINE -> List.of(rampUsers(Config.PIPELINE_USERS_PER_SECOND).during(Duration.ofMinutes(2)));
        };
    }

    public List<ClosedInjectionStep> simulationProfileClosed() {
        if (Config.TEST_TYPE == TestType.PERFORMANCE) {
            return List.of(
                rampConcurrentUsers(0).to(Config.CONSTANT_CONCURRENT_USERS)
                    .during(Duration.ofSeconds(Config.RANK_UP_TIME_SECONDS)),
                constantConcurrentUsers(Config.CONSTANT_CONCURRENT_USERS).during(Config.TEST_DURATION_SECONDS),
                rampConcurrentUsers(Config.CONSTANT_CONCURRENT_USERS).to(0)
                    .during(Duration.ofSeconds(Config.RANK_DOWN_TIME_SECONDS))
            );
        }
        throw new UnsupportedOperationException("Unsupported test type: " + Config.TEST_TYPE);
    }
}

