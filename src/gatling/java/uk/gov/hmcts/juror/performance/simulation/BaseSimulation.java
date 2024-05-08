package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import uk.gov.hmcts.juror.performance.Config;

import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.http.HttpDsl.http;

public class BaseSimulation extends Simulation {
    private final HttpProtocolBuilder httpProtocol;

    protected BaseSimulation() {
        this.httpProtocol = http
            .baseUrl(Config.BASE_URL)
            .doNotTrackHeader("1")
            .inferHtmlResources()
            .silentResources();
    }

    protected void addAssertions(SetUp setUp) {
        setUp.protocols(httpProtocol)
        //TODO re-enable assertions
//            .assertions(
//                //No failed requests
//                global().failedRequests().count().is(0L),
//                //95% of requests should respond within 500ms
//                global().responseTime().percentile3().lte(1500)//TODO confirm
//            )
        ;
    }
}
