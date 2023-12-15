package uk.gov.hmcts.juror.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.Session;
import uk.gov.hmcts.juror.support.generation.generators.value.RandomFromCollectionGeneratorImpl;
import uk.gov.hmcts.juror.support.generation.generators.value.ValueGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.doSwitch;
import static io.gatling.javaapi.core.CoreDsl.feed;

public class Feeders {
    private record User(String username, String password) {
    }

    public static final Map<String, ValueGenerator<User>> USER_FEEDER_MAP;
    public static final FeederBuilder<?> DEFERAL_CODE_FEEDER;
    public static final FeederBuilder<?> EXCUSAL_CODE_FEEDER;
    public static final Map<String, FeederBuilder<Object>> JUROR_NUMBER_FEEDER_BY_STATUS_MAP;
    public static final ChainBuilder JUROR_NUMBER_FEEDER_BY_STATUS;
    public static final FeederBuilder<?> JUROR_NUMBER_FEEDER;

    static {
        JUROR_NUMBER_FEEDER =  Util.jdbcFeeder("select juror_number, owner from juror_mod.juror_pool");
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


        HashMap<String, ValueGenerator<User>> tmpUserList = new HashMap<>();

        Util.OWNER_LIST.forEach(owner -> tmpUserList.put(owner,
            new RandomFromCollectionGeneratorImpl<>(
                Util.jdbcFeeder(
                        "select username, password, owner from juror_mod.users where owner = '" + owner + "'")
                    .readRecords()
                    .stream()
                    .map(stringObjectMap -> new User(
                        String.valueOf(stringObjectMap.get("username")),
                        Util.getPasswordFromDB(String.valueOf(stringObjectMap.get("password"))))
                    ).toList())));

        USER_FEEDER_MAP = Collections.unmodifiableMap(tmpUserList);

        DEFERAL_CODE_FEEDER = Util.listFeeder("exc_code", List.of(
                'A', 'B', 'C', 'F', 'G', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z'))
            .random();
        EXCUSAL_CODE_FEEDER = Util.listFeeder(Util.jdbcFeeder("select exc_code from juror_mod"
            + ".t_exc_code where enabled = true").readRecords()).random();
    }

    private static List<Choice.WithKey> toChoiceList(Map<String, FeederBuilder<Object>> map) {
        return map.entrySet()
            .stream()
            .map(entry -> Choice.withKey(entry.getKey(), feed(entry.getValue())))
            .toList();
    }

    private static FeederBuilder<Object> createJurorNumberFeederByStatus(int status) {
        return Util.jdbcFeeder("select j.juror_number, jp.owner, LOWER(jr.reply_type) as reply_type from juror_mod"
            + ".juror_pool jp  "
            + "join juror_mod.juror j on "
            + "j.juror_number = jp.juror_number "
            + "join juror_mod.juror_response jr on "
            + "jr.juror_number = jp.juror_number "
            + "where jp.status = " + status).random();
    }

    public static Session getUser(Session session) {
        User user = USER_FEEDER_MAP.get(session.getString("owner")).generate();
        return session.set("username", user.username()).set("password", user.password());
    }
}
