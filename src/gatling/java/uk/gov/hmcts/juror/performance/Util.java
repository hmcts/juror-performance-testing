package uk.gov.hmcts.juror.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CheckBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.jdbc.JdbcDsl;
import uk.gov.hmcts.juror.support.generation.generators.value.LocalDateGeneratorImpl;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.doIfEqualsOrElse;
import static io.gatling.javaapi.core.CoreDsl.exec;

public class Util {
    public static final Map<String, String> COMMON_HEADERS;
    public static final Map<String, String> PASSWORD_MAP;

    public static final List<String> OWNER_LIST;

    private static final LocalDateGeneratorImpl LOCAL_DATE_GENERATOR = new LocalDateGeneratorImpl(
        LocalDate.now().plusDays(30),
        LocalDate.now().plusDays(180)
    );

    static {
        COMMON_HEADERS = Map.of(
            "accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,"
                + "application/signed-exchange;v=b3;q=0.9",
            "accept-encoding", "gzip, deflate, br",
            "accept-language", "en-GB,en;q=0.9",
            "sec-fetch-dest", "document",
            "sec-fetch-mode", "navigate",
            "sec-fetch-site", "same-origin",
            "sec-fetch-user", "?1",
            "upgrade-insecure-requests", "1",
            "origin", "null"
        );
        PASSWORD_MAP = Map.of(
            "5BAA61E4C9B93F3F", "password"
        );
        OWNER_LIST = List.of("400", "415");

//TODO uncomment        OWNER_LIST = Util.jdbcFeeder("select distinct owner from juror_mod.court_location")
//            .readRecords()
//            .stream()
//            .map(record -> String.valueOf(record.get("owner")))
//            .toList();
    }

    public static CheckBuilder.Final saveCsrf() {
        return css("input[name='_csrf']", "value").saveAs("csrf");
    }


    public static CheckBuilder.Final validatePageIdentifier(String pageIdentifier) {
        return css("meta[name='pageIdentifier']", "content").is(pageIdentifier);
    }

    public static CheckBuilder.Final validateHeading(String headingValue) {
        return css("h1.govuk-heading-l").is(headingValue);
    }

    public static FeederBuilder<Object> jdbcFeeder(String sql) {
        System.out.println(
            "Creating jdbcFeeder: " + Config.DB_URL + ", " + Config.DB_USERNAME + ", " + Config.DB_PASSWORD + ", "
                + sql);

        return JdbcDsl.jdbcFeeder(Config.DB_URL, Config.DB_USERNAME, Config.DB_PASSWORD, sql);
    }

    public static String getPasswordFromDB(String password) {
        return PASSWORD_MAP.get(password);
    }

    public static ChainBuilder isBureau(ChainBuilder bureauActions,
                                        ChainBuilder courtActions) {
        return doIfEqualsOrElse("#{owner}", "400")
            .then(bureauActions).orElse(courtActions);
    }

    public static String createDateString(DateTimeFormatter dateTimeFormatter) {
        return LOCAL_DATE_GENERATOR.generateValue().format(dateTimeFormatter);
    }

    static int count = 0;
    public static ChainBuilder printSessionVariables() {
        return exec(session -> {
            try {
                count++;
                Field field = Session.class.getDeclaredField("wrapped");
                field.setAccessible(true);
                io.gatling.core.session.Session wrapped = (io.gatling.core.session.Session) field.get(session);
                System.out.println(count + " Session variables: " + wrapped.attributes());
            }catch (Exception e){
                e.printStackTrace();
            }
            return session;
        });
    }
}
