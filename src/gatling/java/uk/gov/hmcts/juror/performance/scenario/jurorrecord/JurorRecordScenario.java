package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class JurorRecordScenario {
    private static final String GROUP_NAME = "Juror Record";

    private static final String BASE_URL = "/juror-management/record/#{juror_number}";

    private JurorRecordScenario() {

    }

    public static ChainBuilder getSummons() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Summons")
            .on(
                exec(
                    http("GET - Juror Record - Summons")
                        .get(BASE_URL + "/summons")
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("juror record - summons"))
                )
            );
    }

    public static ChainBuilder getUpdateRecord() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Update Record")
            .on(
                exec(
                    http("GET - Juror Record - Update Record")
                        .get(JurorRecordUpdateScenario.BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .check(Util.validatePageIdentifier("Update juror record"))
                )
            );
    }
}
