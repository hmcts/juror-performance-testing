package uk.gov.hmcts.juror.performance.scenario.jurorrecord;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.scenario.AvailablePoolsScenario;
import uk.gov.hmcts.juror.support.generation.util.RandomGenerator;

import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.http.HttpDsl.http;

public class JurorRecordPostponeScenario {
    private static final String GROUP_NAME = JurorRecordUpdateScenario.GROUP_NAME + " - postpone";

    private static final String BASE_URL = JurorRecordUpdateScenario.BASE_URL + "/postpone-date";

    public static ChainBuilder postPostponeDate() {
        return group(Util.getNewScenarioId() + GROUP_NAME + " - POST - date")
            .on(
                exec(session -> {
                    String minDate = session.getString("postponeNewServiceStartMinDateStr");

                    assert minDate != null;
                    LocalDate newDate = LocalDate.from(Util.STANDARD_DATE_FORMATTER.parse(minDate))
                        .plusWeeks(RandomGenerator.nextInt(1, 3));
                    return session.set("postponeNewServiceStart", Util.STANDARD_DATE_FORMATTER.format(newDate));
                }).exec(
                    AvailablePoolsScenario.applyChecks(http("POST - Juror Record - Update Record - postpone - date")
                        .post(BASE_URL)
                        .headers(Util.COMMON_HEADERS)
                        .formParam("postponeTo", "#{postponeNewServiceStart}")
                        .formParam("_csrf", "#{csrf}")
                    )
                )
            );
    }
}
