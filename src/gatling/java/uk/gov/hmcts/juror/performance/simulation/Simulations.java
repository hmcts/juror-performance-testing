package uk.gov.hmcts.juror.performance.simulation;

import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.PopulationBuilder;
import lombok.Getter;
import uk.gov.hmcts.juror.performance.Config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

;

@Getter
public enum Simulations {
    ENABLE_JUROR_RECORD_SEARCH(() ->
        //Juror Record Search  -- 200 by 20 per hour (10 per hour per user)
        JurorRecordSearchSimulation.getScenarioBuilderStatic(Config.ENV.getPaceInSeconds(10, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(0))
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_SUMMONS_REPLY_SEARCH(() ->
        //Summons reply search -- 200 by 20 per hour (10 per hour per user)
        SummonsReplySearchSimulation.getScenarioBuilderStatic(Config.ENV.getPaceInSeconds(10, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(10))
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_BULK_CHECKIN_CHECKOUT(() ->
        //Check in & Bulk checkout -- 333 by 20 per hour (17 per hour per user) -- 12 Jurors Per transaction
        RecordAttendanceSimulation.getScenarioBuilderStatic(Config.ENV.getPaceInSeconds(17, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(20))
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_YOUR_WORK(() ->
        //your work 180 by 20 per hour (9 per hour per user)
        YourWorkSimulation.getScenarioBuilderStatic(Config.ENV.getPaceInSeconds(9, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(30))
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_DEFERRALS(() ->
        //deferrals - court -- 180 by 20 per hour (9 per hour per user)
        DeferralSimulation.getCourtScenarioBuilderStatic(Config.ENV.getPaceInSeconds(9, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(40))
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_EXCUSALS(() ->
        //excusal - court -- 250 by 20 per hour (12.5 per hour per user)
        ExcusalSimulation.getCourtScenarioBuilderStatic(Config.ENV.getPaceInSeconds(13, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(50))
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_POSTPONEMENT(() ->
        //postponements  9 by 2 per hour (9 per hour per user)
        PostponeSimulation.getScenarioBuilderStatic(Config.ENV.getPaceInSeconds(9, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(2, Duration.ofSeconds(60))
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_EXPENSES(() ->
        //Expenses -- 333 by 20 per hour (17 per hour per user) -- 12 Jurors Per
        ExpenseSimulation.getScenarioBuilderStatic(Config.ENV.getPaceInSeconds(17, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(70))
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_UTILIZATION_DAILY(() ->
        //utilization report (90 courts @ 2 per court per month) (2 per hour per user - 20 users)
        UtilizationSimulation.getDailyScenarioBuilderStatic(Config.ENV.getPaceInSeconds(2, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(100), 2L)
                    .toArray(new ClosedInjectionStep[0]))),
    ENABLE_UTILIZATION_MONTHLY(() ->
        //utilization report (90 courts @ 2 per court per month) (2 per hour per user - 20 users)
        UtilizationSimulation.getMonthlyScenarioBuilderStatic(Config.ENV.getPaceInSeconds(2, TimeUnit.HOURS))
            .injectClosed(
                Config.ENV.simulationProfileClosed(20, Duration.ofSeconds(130), 2L)
                    .toArray(new ClosedInjectionStep[0])));

    private final Supplier<PopulationBuilder> populationBuilderSupplier;

    Simulations(Supplier<PopulationBuilder> populationBuilderSupplier) {
        this.populationBuilderSupplier = populationBuilderSupplier;
    }

    public PopulationBuilder getPopulationBuilder() {
        return populationBuilderSupplier.get();
    }
}
