package net.syscon.elite.service.impl.keyworker;

import net.syscon.elite.api.model.Keyworker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring managed component responsible for instantiating and initialising {@code KeyworkerPool} instances.
 */
@Component
public class KeyworkerPoolFactory {
    @Value("${api.keyworker.allocation.capacity.tiers}")
    private List<Integer> capacityTiers;

    public KeyworkerPool getKeyworkerPool(List<Keyworker> keyworkers) {
        return new KeyworkerPool(keyworkers, capacityTiers);
    }
}
