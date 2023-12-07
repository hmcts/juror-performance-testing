package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.FeederBuilder;
import uk.gov.hmcts.juror.performance.Util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.doSwitch;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class JurorRecordSearchScenario {
    private static final String GROUP_NAME = "Juror Record Search";

    private static final Map<String, FeederBuilder<Object>> JUROR_NUMBER_BY_OWNER_FEEDER_MAP;
    private static final List<Choice.WithKey> JUROR_NUMBER_BY_OWNER_FEEDER_CHOICE;

    private static final FeederBuilder<Object> JUROR_NUMBER_FEEDER_BUREAU;

    static {
        JUROR_NUMBER_BY_OWNER_FEEDER_MAP =
            Util.OWNER_LIST.stream()
                .collect(
                    Collectors.toMap(owner -> owner,
                        owner -> Util.jdbcFeeder("select juror_number from juror_mod.juror_pool jp  "
                            + "where owner = '" + owner + "'").circular())
                );

        JUROR_NUMBER_BY_OWNER_FEEDER_CHOICE = JUROR_NUMBER_BY_OWNER_FEEDER_MAP.entrySet()
            .stream()
            .map(entry -> Choice.withKey(entry.getKey(), feed(entry.getValue())))
            .toList();

        JUROR_NUMBER_FEEDER_BUREAU = Util.jdbcFeeder("select juror_number from juror_mod.juror_pool");
    }

    public static final ChainBuilder JUROR_RECORD_SEARCH_BUREAU = group(GROUP_NAME).on(
        feed(JUROR_NUMBER_FEEDER_BUREAU)
            .exec(getJurorRecordSearch("Bureau"))
    );

    public static final ChainBuilder JUROR_RECORD_SEARCH_COURT = group(GROUP_NAME).on(
        doSwitch("#{owner}")
            .on(JUROR_NUMBER_BY_OWNER_FEEDER_CHOICE)
            .exec(getJurorRecordSearch("Court")));

    private static ChainBuilder getJurorRecordSearch(String suffix) {
        return exec(
            http("Juror Number Search - " + suffix)
                .post("/juror-record/search")
                .headers(Util.COMMON_HEADERS)
                .check(Util.saveCsrf())
                .formParam("super-nav-search", "#{juror_number}")
                .formParam("_csrf", "#{csrf}")
                .check(Util.validatePageIdentifier("juror record - overview"))
                .check(css("#jurorNumber").isEL("#{juror_number}"))
        );
    }

    private JurorRecordSearchScenario() {

    }
}
