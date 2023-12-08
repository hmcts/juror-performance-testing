package uk.gov.hmcts.juror.performance;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.FeederBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.feed;

public class Feeders {

    public static final Map<String, FeederBuilder<Object>> JUROR_NUMBER_BY_OWNER_FEEDER_MAP;
    public static final List<Choice.WithKey> JUROR_NUMBER_BY_OWNER_FEEDER_CHOICE;

    public static final FeederBuilder<Object> JUROR_NUMBER_FEEDER_BUREAU;

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

        JUROR_NUMBER_FEEDER_BUREAU = Util.jdbcFeeder("select juror_number from juror_mod.juror_pool where owner = "
            + "'400'");
    }
}
