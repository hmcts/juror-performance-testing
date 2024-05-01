package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ScenarioBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;
import uk.gov.hmcts.juror.performance.gen.FeederGenerator;
import uk.gov.hmcts.juror.performance.scenario.JurorExpenseScenario;
import uk.gov.hmcts.juror.performance.scenario.JurorRecordSearchScenario;
import uk.gov.hmcts.juror.performance.scenario.LoginScenario;
import uk.gov.hmcts.juror.performance.scenario.jurorrecord.JurorRecordScenario;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class ExpenseSimulation {

    public static ScenarioBuilder getScenarioBuilderStatic(int secondsPerTransaction) {
        Util.resetCounter();
        return scenario("Expense - Enter for approval")
            .group("Expense - Enter for approval").on(
                feed(Feeders.OWNER_FEEDER_COURT)
                    .exec(LoginScenario.login())
                    .exec(session -> {
                        String owner = session.getString("owner");
                        FeederGenerator feederGenerator = Feeders.getOrJurorNumberExpenseFeeder(owner);
                        return session.set("juror_number_gen", feederGenerator);
                    })
                    .forever().on(
                        pace(Duration.ofSeconds(secondsPerTransaction))
                            .exec(session -> {
                                FeederGenerator gen = Objects.requireNonNull(session.get("juror_number_gen"));
                                Map<String, Object> data = gen.generate();
                                return session
                                    .set("juror_number", data.get("juror_number"))
                                    .set("attendance_date", data.get("attendance_date"))
                                    .set("loc_code", data.get("loc_code"));
                            })
                            .exec(
                                JurorRecordSearchScenario.jurorRecordSearch(),
                                JurorRecordScenario.getExpenses(),
                                JurorExpenseScenario.viewTotalInDraftExpenses(),
                                JurorExpenseScenario.getAddDraftExpense(),
                                JurorExpenseScenario.postAddDraftExpenseSaveAndBack()
                            )
                    ));
    }
}
