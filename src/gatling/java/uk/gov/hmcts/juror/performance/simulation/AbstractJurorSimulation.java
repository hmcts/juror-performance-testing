package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.performance.Config;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.http.HttpDsl.http;

@Slf4j
public abstract class AbstractJurorSimulation extends Simulation {
    private final HttpProtocolBuilder httpProtocol;


    protected AbstractJurorSimulation() {
        this.httpProtocol = http
            .baseUrl(Config.BASE_URL)
            .doNotTrackHeader("1")
            .inferHtmlResources()
            .silentResources();
        setup();
    }

    @Override
    public void before() {
        super.before();
        log.info(Config.asString());
    }

    private ScenarioBuilder getScenarioBuilder() {
        Util.resetCounter();
        return getScenario();
    }

    public void setup() {
        setUp(
            getScenarioBuilder()
                .injectOpen(simulationProfile().toArray(new OpenInjectionStep[0]))
        ).protocols(httpProtocol)
            .assertions(
                //No failed requests
                global().failedRequests().count().is(0L),
                //95% of requests should respond within 500ms
                global().responseTime().percentile3().lte(500)
            );
    }

    protected abstract ScenarioBuilder getScenario();

    public List<OpenInjectionStep> simulationProfile() {
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
}

