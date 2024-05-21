package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Util;

public class NavBarScenario {
    private static final String GROUP_NAME = "Nav Bar";


    public static ChainBuilder getJurorManagement() {
        return Util.group(Util.getNewScenarioId() + GROUP_NAME + " - GET - Juror Management")
            .on(JurorManagementScenario.getManageJurors());//This is the default implementation
    }
}
