package uk.gov.hmcts.juror.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.FeederBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.doSwitch;
import static io.gatling.javaapi.core.CoreDsl.feed;

public class Feeders {

    public static final Map<String, FeederBuilder<Object>> JUROR_NUMBER_BY_OWNER_FEEDER_MAP;
    public static final List<Choice.WithKey> JUROR_NUMBER_BY_OWNER_FEEDER_CHOICE;

    public static final FeederBuilder<Object> JUROR_NUMBER_FEEDER_BUREAU;
    public static final FeederBuilder<?> DEFERAL_CODE_FEEDER;
    public static final FeederBuilder<?> EXCUSAL_CODE_FEEDER;
    public static final ChainBuilder JUROR_NUMBER_MATCHING_OWNER_FEEDER;
    public static final Map<String, FeederBuilder<Object>> JUROR_NUMBER_FEEDER_BY_STATUS_MAP;
    public static final ChainBuilder JUROR_NUMBER_FEEDER_BY_STATUS;

    static {
        JUROR_NUMBER_BY_OWNER_FEEDER_MAP =
            Util.OWNER_LIST.stream()
                .collect(
                    Collectors.toMap(owner -> owner,
                        owner -> Util.jdbcFeeder("select juror_number from juror_mod.juror_pool jp  "
                            + "where owner = '" + owner + "'").circular())
                );

        JUROR_NUMBER_FEEDER_BY_STATUS_MAP = Util.jdbcFeeder("select distinct status from juror_mod.juror_pool")
            .readRecords()
            .stream()
            .map(status -> Map.entry(
                String.valueOf(status.get("status")),
                createJurorNumberFeederByStatus(Integer.parseInt(status.get("status").toString()))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        JUROR_NUMBER_FEEDER_BY_STATUS = doSwitch("#{status}").on(
            toChoiceList(JUROR_NUMBER_FEEDER_BY_STATUS_MAP)
        );

        JUROR_NUMBER_BY_OWNER_FEEDER_CHOICE = toChoiceList(JUROR_NUMBER_BY_OWNER_FEEDER_MAP);

        JUROR_NUMBER_FEEDER_BUREAU = Util.jdbcFeeder("select juror_number from juror_mod.juror_pool where owner = "
            + "'400'");
        DEFERAL_CODE_FEEDER = Util.jdbcFeeder("select exc_code from juror_mod.t_exc_code").random();
        EXCUSAL_CODE_FEEDER = DEFERAL_CODE_FEEDER;

        JUROR_NUMBER_MATCHING_OWNER_FEEDER = doSwitch("#{owner}")
            .on(Feeders.JUROR_NUMBER_BY_OWNER_FEEDER_CHOICE);
    }

    private static List<Choice.WithKey> toChoiceList(Map<String, FeederBuilder<Object>> map) {
        return map.entrySet()
            .stream()
            .map(entry -> Choice.withKey(entry.getKey(), feed(entry.getValue())))
            .toList();
    }

    private static FeederBuilder<Object> createJurorNumberFeederByStatus(int status) {
        return Util.jdbcFeeder("select j.juror_number, jp.owner, jr.reply_type from juror_mod.juror_pool jp  "
            + "join juror_mod.juror j on "
            + "j.juror_number = jp.juror_number "
            + "join juror_mod.juror_response jr on "
            + "jr.juror_number = jp.juror_number "
            + "where jp.status = " + status
            + " and jp.owner = '400'");
    }
}
