package uk.gov.hmcts.juror.performance;

import lombok.SneakyThrows;

import java.util.Optional;

public class Config {
    public static final boolean INCLUDE_GROUPS = false;
    public static final String BASE_URL;
    public static final TestType TEST_TYPE;
    public static final boolean DEBUG;
    public static final long RANK_UP_TIME_SECONDS;
    public static final long TEST_DURATION_SECONDS;
    public static final long RANK_DOWN_TIME_SECONDS;
    public static final int USERS_PER_SECOND;
    public static final int CONSTANT_CONCURRENT_USERS;
    public static final int PIPELINE_USERS_PER_SECOND;
    public static final String ENVIRONMENT;
    public static final String DB_URL;
    public static final String DB_USERNAME;
    public static final String DB_PASSWORD;
    public static final int REQUESTS_PER_SECOND;
    private static final double REQUESTS_PER_SECOND_PER_USER;


    static {
        BASE_URL = getProperty("BASE_URL", "http://0.0.0.0:3000");
        TEST_TYPE =
            TestType.valueOf(getProperty("TEST_URL", TestType.PERFORMANCE.name()));
        DEBUG =
            Boolean.parseBoolean(getProperty("DEBUG", "false"));
        ENVIRONMENT = getProperty("ENVIRONMENT", TEST_TYPE.name());

        DB_URL = getProperty("DB_URL", "jdbc:postgresql://localhost:5432/juror");
        DB_USERNAME = getProperty("DB_USERNAME", "system");
        DB_PASSWORD = getProperty("DB_PASSWORD", "postgres");


        RANK_UP_TIME_SECONDS = Long.parseLong(getProperty("RANK_UP_TIME_SECONDS", "120"));
        RANK_DOWN_TIME_SECONDS = Long.parseLong(getProperty("RANK_DOWN_TIME_SECONDS", "120"));
        TEST_DURATION_SECONDS = Long.parseLong(getProperty("TEST_DURATION_SECONDS", "600"));
        PIPELINE_USERS_PER_SECOND = Integer.parseInt(getProperty("PIPELINE_USERS_PER_SECOND", "10"));

        if (hasProperty("USERS_PER_HOUR")) {
            USERS_PER_SECOND = Integer.parseInt((Double.parseDouble(getProperty("USERS_PER_HOUR", "3600")) / 3600.0) +
                "");
        } else {
            USERS_PER_SECOND = Integer.parseInt(getProperty("USERS_PER_SECOND", "1"));
        }
        if (hasProperty("REQUESTS_PER_HOUR_PER_USER")) {
            REQUESTS_PER_SECOND_PER_USER =
                Double.parseDouble(getProperty("REQUESTS_PER_HOUR_PER_USER","3600")) / 3600.0;
        } else {
            REQUESTS_PER_SECOND_PER_USER = Double.parseDouble(getProperty("REQUESTS_PER_SECOND_PER_USER", "1"));
        }
        CONSTANT_CONCURRENT_USERS = Integer.parseInt(getProperty("CONSTANT_CONCURRENT_USERS", "1"));
        REQUESTS_PER_SECOND = Math.max(1, (int) (REQUESTS_PER_SECOND_PER_USER * USERS_PER_SECOND));
    }

    private static boolean hasProperty(String name) {
        return System.getProperty(name) != null || System.getenv(name) != null;
    }

    private static String getProperty(String name, String defaultValue) {
        String value = System.getProperty(name);
        if (value != null) {
            return value;
        }
        return Optional.ofNullable(System.getenv(name)).orElse(defaultValue);
    }


    @SneakyThrows
    public static String asString() {
        StringBuilder builder = new StringBuilder();
        addValueToBuilder(builder, "Test Type", TEST_TYPE.name());
        addValueToBuilder(builder, "Debug", String.valueOf(DEBUG));
        addValueToBuilder(builder, "Rank Up Time Seconds", String.valueOf(RANK_UP_TIME_SECONDS));
        addValueToBuilder(builder, "Test Duration Seconds", String.valueOf(TEST_DURATION_SECONDS));
        addValueToBuilder(builder, "Rank Down Time Seconds", String.valueOf(RANK_DOWN_TIME_SECONDS));
        addValueToBuilder(builder, "Users Per Second", String.valueOf(USERS_PER_SECOND));
        addValueToBuilder(builder, "Constant concurrent users", String.valueOf(CONSTANT_CONCURRENT_USERS));
        addValueToBuilder(builder, "Pipeline Users Per Second", String.valueOf(PIPELINE_USERS_PER_SECOND));
        return builder.toString();
    }

    private static void addValueToBuilder(StringBuilder builder, String key, String value) {
        builder.append(key).append(": ").append(value).append("\n");
    }
}
