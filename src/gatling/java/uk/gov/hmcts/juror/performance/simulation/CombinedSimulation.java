package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ClosedInjectionStep;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.rampConcurrentUsers;

public class CombinedSimulation extends BaseSimulation {

    private final static int TOTAL_TEST_TIME_SECONDS = 10; //Minus any ramp up/down time
    private final static int RAMP_TIME_SECONDS = 1;


    public CombinedSimulation() {
        super();
        setup();
    }

    public void setup() {
        addAssertions(
            setUp(
                //Juror Record Search  -- 200 by 20 per hour (10 per hour per user)
//                JurorRecordSearchSimulation.getScenarioBuilderStatic(perUnitTime(10, TimeUnit.HOURS))
//                    .injectClosed(
//                        simulationProfileClosed(20)
//                            .toArray(new ClosedInjectionStep[0])),
                //Summons reply search -- 200 by 20 per hour (10 per hour per user)
//                SummonsReplySearchSimulation.getScenarioBuilderStatic(perUnitTime(10, TimeUnit.HOURS))
//                    .injectClosed(
//                        simulationProfileClosed(20)
//                            .toArray(new ClosedInjectionStep[0])),
                //Check in & Bulk checkout -- 333 by 20 per hour (17 per hour per user) -- 12 Jurors Per
//                RecordAttendanceSimulation.getScenarioBuilderStatic(perUnitTime(17, TimeUnit.HOURS))
//                    .injectClosed(
//                        simulationProfileClosed(20)
//                            .toArray(new ClosedInjectionStep[0])),
                // transaction
                //your work 180 by 20 per hour (9 per hour per user)
//                YourWorkSimulation.getScenarioBuilderStatic(perUnitTime(9, TimeUnit.HOURS))
//                    .injectClosed(
//                        simulationProfileClosed(20)
//                            .toArray(new ClosedInjectionStep[0])),
                //deferrals - court -- 180 by 20 per hour (9 per hour per user)
//                DeferralSimulation.getCourtScenarioBuilderStatic(perUnitTime(9, TimeUnit.HOURS))
//                    .injectClosed(
//                        simulationProfileClosed(20)
//                            .toArray(new ClosedInjectionStep[0])),
                //excusal - court -- 250 by 20 per hour (12.5 per hour per user)
//                ExcusalSimulation.getCourtScenarioBuilderStatic(perUnitTime(13, TimeUnit.HOURS))
//                    .injectClosed(
//                        simulationProfileClosed(20)
//                            .toArray(new ClosedInjectionStep[0])),
                //postponements  9 by 2 per hour (9 per hour per user)
//                PostponeSimulation.getScenarioBuilderStatic(perUnitTime(9, TimeUnit.HOURS))
//                    .injectClosed(
//                        simulationProfileClosed(2)
//                            .toArray(new ClosedInjectionStep[0])),
                //TODO utilization report (90 courts @ 2 per court per month) (2 per hour per user - 20 users)
                //TODO expenses tbc

            ).maxDuration(Duration.ofSeconds(TOTAL_TEST_TIME_SECONDS + (RAMP_TIME_SECONDS * 2)))
        );
    }

    private int perUnitTime(int count, TimeUnit timeUnit) {
        return (int) timeUnit.toSeconds(1) / count;
    }


    private List<ClosedInjectionStep> simulationProfileClosed(int constantConcurrentUsers) {
        return simulationProfileClosed(constantConcurrentUsers, RAMP_TIME_SECONDS, TOTAL_TEST_TIME_SECONDS,
            RAMP_TIME_SECONDS);
    }

    private List<ClosedInjectionStep> simulationProfileClosed(int constantConcurrentUsers, int rampUpTimeSeconds,
                                                              int testDurationSeconds, int rampDownTimeSeconds) {
        return List.of(
            rampConcurrentUsers(0).to(constantConcurrentUsers)
                .during(Duration.ofSeconds(rampUpTimeSeconds)),
            constantConcurrentUsers(constantConcurrentUsers).during(Duration.ofSeconds(testDurationSeconds)),
            rampConcurrentUsers(constantConcurrentUsers).to(0)
                .during(Duration.ofSeconds(rampDownTimeSeconds))
        );
    }
}
