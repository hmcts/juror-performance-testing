package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class LoginScenario {
    private static final String GROUP_NAME = "Login";
    private static final String LOGIN_URL = "/";

    private LoginScenario() {

    }

    public static ChainBuilder getLoginScreen() {
        return getLoginScreen(Util.getNewScenarioId());
    }

    public static ChainBuilder getLoginScreen(String scenarioId) {
        return group(scenarioId + GROUP_NAME + " - GET - Login Screen")
            .on(
                exec(
                    http("GET - Login Screen")
                        .get(LOGIN_URL)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("sign in"))
                        .check(Util.saveCsrf())
                )
            );
    }

    public static ChainBuilder postLoginCourt() {
        return postLoginCourt(Util.getNewScenarioId());
    }

    public static ChainBuilder postLoginCourt(String scenarioId) {
        return group(scenarioId + GROUP_NAME + " - POST - Login (Court)")
            .on(exec(Feeders::getUser)
                .exec(
                    http("POST - Login (Court)")
                        .post(LOGIN_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("userID", "#{username}")
                        .formParam("password", "#{password}")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("Homepage"))
                )
            );
    }

    public static ChainBuilder postLoginBureau() {
        return postLoginBureau(Util.getNewScenarioId());
    }

    public static ChainBuilder postLoginBureau(String scenarioId) {
        return group(scenarioId + GROUP_NAME + " - POST - Login (Bureau)")
            .on(exec(Feeders::getUser)
                .exec(
                    http("POST - Login (Bureau)")
                        .post(LOGIN_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("userID", "#{username}")
                        .formParam("password", "#{password}")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.validatePageIdentifier("your work - to do"))
                )
            );
    }

    public static ChainBuilder loginAsCourt() {
        return loginAsCourt(Util.getNewScenarioId(), Util.getNewScenarioId());
    }

    public static ChainBuilder loginAsCourt(String getScenarioId, String postScenarioId) {
        return exec(getLoginScreen(getScenarioId), postLoginCourt(postScenarioId));
    }

    public static ChainBuilder loginAsBureau() {
        return loginAsBureau(Util.getNewScenarioId(), Util.getNewScenarioId());
    }

    public static ChainBuilder loginAsBureau(String getScenarioId, String postScenarioId) {
        return exec(getLoginScreen(getScenarioId), postLoginBureau(postScenarioId));
    }

    public static ChainBuilder login() {
        String getScenarioId = Util.getNewScenarioId();
        String postScenarioId = Util.getNewScenarioId();
        return Util.isBureau(loginAsBureau(getScenarioId, postScenarioId), loginAsCourt(getScenarioId, postScenarioId));
    }
}
