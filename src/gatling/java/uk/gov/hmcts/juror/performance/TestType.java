package uk.gov.hmcts.juror.performance;

import lombok.Getter;
import uk.gov.hmcts.juror.performance.env.Environment;
import uk.gov.hmcts.juror.performance.env.LocalEnvironment;
import uk.gov.hmcts.juror.performance.env.PVTEnvironment;

import java.util.function.Supplier;

@Getter
public enum TestType {
    PERFORMANCE(PVTEnvironment::new),
    LOCAL(LocalEnvironment::new),
    PIPELINE(PVTEnvironment::new);

    final Supplier<Environment> environmentSupplier;

    TestType(Supplier<Environment> environmentSupplier) {
        this.environmentSupplier = environmentSupplier;
    }
}
