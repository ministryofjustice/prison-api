package net.syscon.elite.service.impl.keyworker;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

/**
 * Spring managed component responsible for instantiating and initialising {@code KeyworkerPool} instances.
 */
@Component
@Slf4j
public class KeyworkerPoolFactory {
    private final KeyWorkerAllocationService keyWorkerAllocationService;

    @Value("${api.keyworker.allocation.capacity.tiers:6,9}")
    private Set<Integer> capacityTiers;

    public KeyworkerPoolFactory(KeyWorkerAllocationService keyWorkerAllocationService) {
        this.keyWorkerAllocationService = keyWorkerAllocationService;
    }

    /**
     * Initialise new key worker pool with set of key workers and capacity tiers.
     *
     * @param keyworkers set of key workers in the pool.
     * @return initialised key worker pool.
     */
    public KeyworkerPool getKeyworkerPool(Collection<Keyworker> keyworkers) {
        Validate.notEmpty(keyworkers);

        KeyworkerPool keyworkerPool = new KeyworkerPool(keyworkers, capacityTiers);

        keyworkerPool.setKeyWorkerAllocationService(keyWorkerAllocationService);

        log.debug("Initialised new Key worker pool with {} members and {} capacity tiers.",
                keyworkers.size(), capacityTiers.size());

        return keyworkerPool;
    }
}
