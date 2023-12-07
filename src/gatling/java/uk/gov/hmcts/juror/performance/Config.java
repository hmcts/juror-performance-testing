package uk.gov.hmcts.juror.performance;

import lombok.SneakyThrows;

public class Config {
    public static final String BASE_URL;
    public static final TestType TEST_TYPE;
    public static final boolean DEBUG;
    public static final long RANK_UP_TIME_SECONDS;
    public static final long TEST_DURATION_SECONDS;
    public static final long RANK_DOWN_TIME_SECONDS;
    public static final double USERS_PER_SECOND;
    public static final int PIPELINE_USERS_PER_SECOND;
    public static final String ENVIRONMENT;
    public static final String DB_URL;
    public static final String DB_USERNAME;
    public static final String DB_PASSWORD;
    public static final int REQUESTS_PER_SECOND;
    private static final double REQUESTS_PER_SECOND_PER_USER;

    static {
        BASE_URL =
            System.getProperty("TEST_URL", "http://0.0.0.0:3000");
        TEST_TYPE =
            TestType.valueOf(System.getProperty("TEST_URL", TestType.PERFORMANCE.name()));
        DEBUG =
            Boolean.parseBoolean(System.getProperty("DEBUG", "false"));
        ENVIRONMENT = System.getProperty("ENVIRONMENT", TEST_TYPE.name());

        DB_URL = System.getProperty("DB_URL", "jdbc:postgresql://localhost:5432/juror");
        DB_USERNAME = System.getProperty("DB_USERNAME", "system");
        DB_PASSWORD = System.getProperty("DB_PASSWORD", "postgres");


        RANK_UP_TIME_SECONDS = Long.parseLong(System.getProperty("RANK_UP_TIME_SECONDS", "120"));
        RANK_DOWN_TIME_SECONDS = Long.parseLong(System.getProperty("RANK_DOWN_TIME_SECONDS", "120"));
        TEST_DURATION_SECONDS = Long.parseLong(System.getProperty("TEST_DURATION_SECONDS", "600"));
        PIPELINE_USERS_PER_SECOND = Integer.parseInt(System.getProperty("PIPELINE_USERS_PER_SECOND", "10"));

        if (System.getProperty("USERS_PER_HOUR") != null) {
            USERS_PER_SECOND = Double.parseDouble(System.getProperty("USERS_PER_HOUR")) / 3600.0;
        } else {
            USERS_PER_SECOND = Double.parseDouble(System.getProperty("USERS_PER_SECOND", "1"));
        }
        if (System.getProperty("REQUESTS_PER_HOUR_PER_USER") != null) {
            REQUESTS_PER_SECOND_PER_USER =
                Double.parseDouble(System.getProperty("REQUESTS_PER_HOUR_PER_USER")) / 3600.0;
        } else {
            REQUESTS_PER_SECOND_PER_USER = Double.parseDouble(System.getProperty("REQUESTS_PER_SECOND_PER_USER", "1"));
        }
        REQUESTS_PER_SECOND = Math.max(1, (int) (REQUESTS_PER_SECOND_PER_USER * USERS_PER_SECOND));
    }


    @SneakyThrows
    public static String asString() {
        StringBuilder builder = new StringBuilder();

//        addValueToBuilder(builder, "Base Url", BASE_URL);
        addValueToBuilder(builder, "Test Type", TEST_TYPE.name());
        addValueToBuilder(builder, "Debug", String.valueOf(DEBUG));
        addValueToBuilder(builder, "Rank Up Time Seconds", String.valueOf(RANK_UP_TIME_SECONDS));
        addValueToBuilder(builder, "Test Duration Seconds", String.valueOf(TEST_DURATION_SECONDS));
        addValueToBuilder(builder, "Rank Down Time Seconds", String.valueOf(RANK_DOWN_TIME_SECONDS));
        addValueToBuilder(builder, "Users Per Second", String.valueOf(USERS_PER_SECOND));
        addValueToBuilder(builder, "Pipeline Users Per Second", String.valueOf(PIPELINE_USERS_PER_SECOND));
//        addValueToBuilder(builder, "Environment", ENVIRONMENT);
//        addValueToBuilder(builder, "DB URL", DB_URL);
//        addValueToBuilder(builder, "DB Username", DB_USERNAME);
//        addValueToBuilder(builder, "DB Password", DB_PASSWORD);

        return builder.toString();
    }

    private static void addValueToBuilder(StringBuilder builder, String key, String value) {
        builder.append(key).append(": ").append(value).append("\n");
    }
}
