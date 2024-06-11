package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public final class LoginScenario {
    private static final String GROUP_NAME = "Login";
    private static final String LOGIN_URL_GET = "/";
    private static final String LOGIN_URL_POST = "/auth/dev/email";
    private static final String COURT_SELECT_URL = "/auth/courts-list";

    private static final String SELECT_COURT_PAGE_IDENTIFIER = "Authentication - Select court to manage";

    private LoginScenario() {

    }

    public static ChainBuilder getLoginScreen() {
        return getLoginScreen(Util.getNewScenarioId());
    }

    public static ChainBuilder getLoginScreen(String scenarioId) {
        return Util.group(scenarioId + GROUP_NAME + " - GET - Login Screen")
            .on(
                exec(
                    http("GET - Login Screen")
                        .get(LOGIN_URL_GET)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("Sign in"))
                        .check(Util.saveCsrf())
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
            );
    }

    public static ChainBuilder postLoginCourt() {
        return postLoginCourt(Util.getNewScenarioId());
    }

    public static ChainBuilder postLoginCourt(String scenarioId) {
        return postLogin(scenarioId, "Homepage");
    }

    public static ChainBuilder postLogin(String scenarioId, String expectedEndPage) {
        return Util.group(scenarioId + GROUP_NAME + " - POST - Login")
            .on(exec(Feeders::getUser)
                .exec(
                    http("POST - Login")
                        .post(LOGIN_URL_POST)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("email", "#{username}@justice.gov.uk")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.savePageIdentifier())
                )
                .pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS))
                .exec(Util::printBody)
                .doIfEqualsOrElse("#{pageIdentifier}", SELECT_COURT_PAGE_IDENTIFIER)
                .then(exec(
                    http("POST - Login - Court Selection")
                        .post(COURT_SELECT_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("email", "#{username}@justice.gov.uk")
                        .formParam("court", "#{owner}")
                        .formParam("_csrf", "#{csrf}")
                        .check(Util.savePageIdentifier())
                ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS)))
                .orElse(exec(session -> {
                    if (!Objects.equals(session.getString("pageIdentifier"), expectedEndPage)) {
                        return session.markAsFailed();
                    }
                    return session;
                }))
            );
    }

    public static ChainBuilder postLoginBureau() {
        return postLoginBureau(Util.getNewScenarioId());
    }

    public static ChainBuilder postLoginBureau(String scenarioId) {
        return postLogin(scenarioId, "Your work - To do");
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
        return Util.isBureau(loginAsBureau(getScenarioId, postScenarioId), loginAsCourt(getScenarioId, postScenarioId))
            .exitHereIfFailed();
    }
}
