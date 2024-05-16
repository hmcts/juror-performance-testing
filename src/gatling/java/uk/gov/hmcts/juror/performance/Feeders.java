package uk.gov.hmcts.juror.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.jdbc.JdbcDsl;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.performance.gen.FeederGenerator;
import uk.gov.hmcts.juror.support.generation.generators.code.Generator;
import uk.gov.hmcts.juror.support.generation.generators.value.LoopFromCollectionGeneratorImpl;
import uk.gov.hmcts.juror.support.generation.generators.value.RandomFromCollectionGeneratorImpl;
import uk.gov.hmcts.juror.support.generation.generators.value.ValueGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.onCase;

@Slf4j
public class Feeders {
    private static final Map<String, FeederGenerator> JUROR_NUMBER_EXPENSE_FEEDER = new HashMap<>();


    private static final Map<String, FeederGenerator> JUROR_NUMBER_FEEDER_BY_OWNER_MAP = new HashMap<>();
    public static final Generator<String> YES_NO_GEN = new RandomFromCollectionGeneratorImpl<>("yes", "no");

    public static FeederGenerator getOrCreateByOwnerAndStatus(String owner, String number) {
        final String key = owner + "-" + number;
        if (!JUROR_NUMBER_FEEDER_BY_OWNER_MAP.containsKey(key)) {
            JUROR_NUMBER_FEEDER_BY_OWNER_MAP.put(key, new FeederGenerator(
                jdbcFeeder("select distinct jp.juror_number as juror_number from juror_mod.juror_pool jp "
                    + "join juror_mod.pool p on p.pool_no = jp.pool_number and p.loc_code = '" + owner + "' "
                    + "left join juror_mod.appearance a "
                    + "on a.juror_number = jp.juror_number and a.attendance_date = current_date "
                    + "where jp.owner = '" + owner + "' "
                    + "and p.loc_code = '" + owner + "' "
                    + "and a.juror_number is null "
                    + "and jp.status = " + number + " and jp.juror_number::decimal < 300000000"),
                "juror_number"));
        }
        return JUROR_NUMBER_FEEDER_BY_OWNER_MAP.get(key);
    }


    public static FeederGenerator getOrJurorNumberExpenseFeeder(String owner) {
        if (!JUROR_NUMBER_EXPENSE_FEEDER.containsKey(owner)) {
            JUROR_NUMBER_EXPENSE_FEEDER.put(owner, new FeederGenerator(
                jdbcFeeder("select a.juror_number as juror_number, a.attendance_date as attendance_date,"
                    + " a.loc_code as loc_code from juror_mod.appearance a "
                    + "where a.loc_code =  '" + owner + "' "
                    + "and a.appearance_stage = 'EXPENSE_ENTERED' "
                    + "and a.is_draft_expense = true"
                    + " and a.juror_number::decimal < 300000000")
                    .queue(),
                "juror_number", "attendance_date", "loc_code"));
        }
        return JUROR_NUMBER_EXPENSE_FEEDER.get(owner);
    }

    private record User(String username) {
    }

    public static final Map<String, ValueGenerator<User>> USER_FEEDER_MAP;
    public static final FeederBuilder<?> DEFERAL_CODE_FEEDER;
    public static final FeederBuilder<?> EXCUSAL_CODE_FEEDER;
    public static final Map<String, FeederBuilder<Object>> JUROR_NUMBER_FEEDER_BY_STATUS_MAP;
    public static final ChainBuilder JUROR_NUMBER_FEEDER_BY_STATUS;
    public static final FeederBuilder<?> JUROR_NUMBER_FEEDER;

    public static final FeederBuilder<?> JUROR_NUMBER_RESPONSE_FEEDER;

    private static final Set<String> OWNER_LIST;

    public static final FeederBuilder<Object> OWNER_FEEDER;

    public static final FeederBuilder<Object> OWNER_FEEDER_COURT;

    public static final FeederBuilder<Object> OWNER_FEEDER_BUREAU;

    public static final FeederGenerator JUROR_NUMBER_REPLY_TYPE_BUREAU_FEEDER;

    static {
        JUROR_NUMBER_FEEDER = null;
//        jdbcFeeder("select juror_number, owner from juror_mod.juror_pool "
//            + "order by RANDOM()").random();

        JUROR_NUMBER_FEEDER_BY_STATUS_MAP = null;
//            jdbcFeederCached("select distinct status from juror_mod.juror_pool")
//            .readRecords()
//            .stream()
//            .map(status -> Map.entry(
//                String.valueOf(status.get("status")),
//                createJurorNumberFeederByStatus(Integer.parseInt(status.get("status").toString()))))
//            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        JUROR_NUMBER_FEEDER_BY_STATUS = null;
//        doSwitch("#{status}").on(
//            toChoiceList(JUROR_NUMBER_FEEDER_BY_STATUS_MAP)
//        );


        HashMap<String, ValueGenerator<User>> tmpUserList = new HashMap<>();


        JUROR_NUMBER_RESPONSE_FEEDER =
            jdbcFeeder("select jr.juror_number, jr.last_name, jp.pool_number"
                + " from juror_mod.juror_response jr join juror_mod.juror_pool jp on jr.juror_number = jp"
                + ".juror_number where jp.juror_number::decimal < 300000000").random();

        OWNER_LIST = Feeders.jdbcFeederCached("select distinct * from juror_mod.user_courts uc "
                + "join juror_mod.users u on u.username = uc.username "
                + "join juror_mod.court_location cl on cl.owner = cl.loc_code and uc.loc_code = cl.loc_code "
                + "where u.user_type <> 'ADMINISTRATOR'")
            .readRecords()
            .stream()
            .map(stringObjectMap -> String.valueOf(stringObjectMap.get("loc_code")))
            .collect(Collectors.toSet());

        OWNER_LIST.forEach(owner -> tmpUserList.put(owner,
            new LoopFromCollectionGeneratorImpl<>(
                jdbcFeederCached(
                    "select username, owner from juror_mod.users u where "
                        + "u.username in (select uc.username from juror_mod.user_courts uc where uc.username = u"
                        + ".username"
                        + " and uc.loc_code = '" + owner + "')"
                        + " and u.user_type <> 'ADMINISTRATOR'")
                    .readRecords()
                    .stream()
                    .map(stringObjectMap -> String.valueOf(stringObjectMap.get("username")))
                    .distinct()
                    .map(User::new
                    ).toList())));

        USER_FEEDER_MAP = Collections.unmodifiableMap(tmpUserList);

        DEFERAL_CODE_FEEDER = listFeeder("exc_code", List.of(
            'A', 'B', 'C', 'F', 'G', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z'))
            .random();
        EXCUSAL_CODE_FEEDER =
            listFeeder("exc_code", List.of(
                "K", "D", "S", "F", "P", "R", "C", "W", "M", "T", "A", "O", "B", "L", "Y", "X", "G",
                "Z", "J", "I", "N", "PE", "CE", "DC")).random();

        OWNER_FEEDER = listFeeder("owner", OWNER_LIST.stream().toList()).random();

        OWNER_FEEDER_COURT = listFeeder("owner", OWNER_LIST.stream().filter(
            string -> !string.equals("400")).toList()).random();

        OWNER_FEEDER_BUREAU = listFeeder("owner", OWNER_LIST.stream().filter(
            string -> string.equals("400")).toList()).random();
        ;

        JUROR_NUMBER_REPLY_TYPE_BUREAU_FEEDER = new FeederGenerator(
            jdbcFeeder(
                "select jp.juror_number as juror_number, jr.reply_type as reply_type from juror_mod"
                    + ".juror_pool jp"
                    + " join juror_mod.juror_response jr on jr.juror_number = jp.juror_number"
                    + " where jp.status = 2"
                    + " and jp.juror_number::decimal < 300000000")
                .random(), "juror_number", "reply_type");


        OWNER_LIST
            .stream()
            .filter(s -> !s.equalsIgnoreCase("400"))
            .forEach(s -> {
                getOrCreateByOwnerAndStatus(s, "2");
            });
    }

    private static List<Choice.WithKey> toChoiceList(Map<String, FeederBuilder<Object>> map) {
        return map.entrySet()
            .stream()
            .map(entry -> onCase(entry.getKey()).then(feed(entry.getValue())))
            .toList();
    }

    private static FeederBuilder<Object> createJurorNumberFeederByStatus(int status) {
        return jdbcFeeder("select j.juror_number, jp.owner, LOWER(jr.reply_type) as reply_type from juror_mod"
            + ".juror_pool jp  "
            + "join juror_mod.juror j on "
            + "j.juror_number = jp.juror_number "
            + "join juror_mod.juror_response jr on "
            + "jr.juror_number = jp.juror_number "
            + "where jp.status = " + status
            + " order by RANDOM()").random();
    }

    public static Session getUser(Session session) {
        User user = USER_FEEDER_MAP.get(session.getString("owner")).generate();
        return session.set("username", user.username());
    }

    public static FeederBuilder<Object> jdbcFeeder(String sql) {
        log.info(
            "Creating jdbcFeeder: " + Config.DB_URL + ", " + Config.DB_USERNAME + ", " + sql);

        return JdbcDsl.jdbcFeeder(Config.DB_URL, Config.DB_USERNAME, Config.DB_PASSWORD, sql);
    }

    public static FeederBuilder<Object> jdbcFeederCached(String sql) {
        log.info(
            "Creating jdbcFeederCached: " + Config.DB_URL + ", " + Config.DB_USERNAME + ", " + Config.DB_PASSWORD + ", "
                + sql);

        return listFeeder(
            JdbcDsl.jdbcFeeder(Config.DB_URL, Config.DB_USERNAME, Config.DB_PASSWORD, sql).readRecords());
    }

    public static FeederBuilder<Object> listFeeder(String key, List<?> items) {
        return CoreDsl.listFeeder(items.stream()
            .map(item -> (Object) item)
            .map(item -> Map.of(key, item))
            .toList());
    }

    public static FeederBuilder<Object> listFeeder(List<Map<String, Object>> items) {
        return CoreDsl.listFeeder(items);
    }
}
