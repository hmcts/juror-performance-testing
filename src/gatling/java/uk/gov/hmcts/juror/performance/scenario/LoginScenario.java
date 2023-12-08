package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import uk.gov.hmcts.juror.performance.Util;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;

public class LoginScenario {
    private static final String GROUP_NAME = "Login";
    private static final String LOGIN_URL = "/";
    private static final FeederBuilder<Object> COURT_USER_FEEDER =
        Util.jdbcFeeder("select username, password, owner from juror_mod.users where owner != '400'")
            .transform((key, value) -> "password".equals(key) ? Util.getPasswordFromDB(String.valueOf(value)) : value)
            .circular();
    private static final FeederBuilder<Object> BUREAU_USER_FEEDER =
        Util.jdbcFeeder("select username, password, owner from juror_mod.users where owner = '400'")
            .transform((key, value) -> "password".equals(key) ? Util.getPasswordFromDB(String.valueOf(value)) : value)
            .circular();
    public static final ChainBuilder GET_LOGIN_SCREEN =
        group(GROUP_NAME)
            .on(
                exec(
                    http("GET - Login Screen")
                        .get(LOGIN_URL)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("sign in"))
                        .check(Util.saveCsrf())
                ).exitHereIfFailed()
            );

    public static final ChainBuilder POST_LOGIN_COURT =
        group(GROUP_NAME)
            .on(
                feed(COURT_USER_FEEDER)
                    .exec(
                        http("POST - Login (Court)")
                            .post(LOGIN_URL)
                            .headers(Util.COMMON_HEADERS)
                            .formParam("userID", "#{username}")
                            .formParam("password", "#{password}")
                            .formParam("_csrf", "#{csrf}")
                            .check(Util.validatePageIdentifier("Homepage"))
                            .check(Util.saveSessionId())
                    ).exitHereIfFailed()
            );
    public static final ChainBuilder POST_LOGIN_BUREAU =
        group(GROUP_NAME)
            .on(
                feed(BUREAU_USER_FEEDER)
                    .exec(
                        http("POST - Login (Bureau)")
                            .post(LOGIN_URL)
                            .headers(Util.COMMON_HEADERS)
                            .formParam("userID", "#{username}")
                            .formParam("password", "#{password}")
                            .formParam("_csrf", "#{csrf}")
                            .check(Util.validatePageIdentifier("your work - to do"))
                            .check(Util.saveSessionId())
                    ).exitHereIfFailed()
            );

    public static final ChainBuilder LOGIN_AS_COURT = exec(GET_LOGIN_SCREEN, POST_LOGIN_COURT);
    public static final ChainBuilder LOGIN_AS_BUREAU = exec(GET_LOGIN_SCREEN, POST_LOGIN_BUREAU);

    private LoginScenario() {

    }
}
