package uk.gov.hmcts.juror.performance.gen;

import io.gatling.javaapi.core.FeederBuilder;
import uk.gov.hmcts.juror.support.generation.generators.code.Generator;

import java.util.HashMap;
import java.util.Map;

public class FeederGenerator extends Generator<Map<String, Object>> {

    private final String[] keys;
    scala.collection.Iterator<scala.collection.immutable.Map<String, Object>> feeder;

    public FeederGenerator(FeederBuilder<?> feederBuilder, String... keys) {
        super();
        feeder = feederBuilder.asScala().apply();
        this.keys = keys;
    }

    @Override
    public Map<String, Object> generate() {
        scala.collection.immutable.Map<String, Object> next = feeder.next();
        HashMap<String, Object> data = new HashMap<>();
        for (String k : keys) {
            data.put(k, next.get(k).get());
        }
        return data;
    }
}
