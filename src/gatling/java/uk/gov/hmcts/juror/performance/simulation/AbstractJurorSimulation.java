package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import uk.gov.hmcts.juror.performance.Config;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.holdFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.reachRps;
import static io.gatling.javaapi.http.HttpDsl.http;

public abstract class AbstractJurorSimulation extends Simulation {
    private final HttpProtocolBuilder httpProtocol;


    public AbstractJurorSimulation() {
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
        System.out.println(Config.asString());
    }

    public void setup() {
        setUp(
            getScenario()
                .injectOpen(simulationProfile().toArray(new OpenInjectionStep[0]))
//                .throttle(
//                    reachRps(Config.REQUESTS_PER_SECOND).in(
//                        Duration.ofSeconds(Config.RANK_UP_TIME_SECONDS)
//                    ),
//                    holdFor(Duration.ofSeconds(Config.TEST_DURATION_SECONDS)),
//                    reachRps(0).in(Duration.ofSeconds(Config.RANK_DOWN_TIME_SECONDS))
//                )
        ).protocols(httpProtocol)
            .assertions(
                //No failed requests
                global().failedRequests().count().is(0L),
                //95% of requests should be below respond within 500ms
                global().responseTime().percentile3().lt(500)
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

