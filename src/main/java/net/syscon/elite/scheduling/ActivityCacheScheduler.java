package net.syscon.elite.scheduling;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.WhereaboutsEnabledService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class ActivityCacheScheduler {

    private final AgencyService agencyService;
    private final WhereaboutsEnabledService whereaboutsEnabledService;

    public ActivityCacheScheduler(final AgencyService agencyService, final WhereaboutsEnabledService whereaboutsEnabledService) {
        this.agencyService = agencyService;
        this.whereaboutsEnabledService = whereaboutsEnabledService;
    }

    //    @Scheduled(fixedRate = 10 * 60 * 1000, initialDelay = 30000)
    // Removed schedule as no long needed, but left in for now just in case...
    public void cacheActivityLocations() {

        final var start = System.currentTimeMillis();
        log.info("START: cacheActivityLocations");
        // get all prisons
        final var prisons = agencyService.getAgenciesByType("INST");

        prisons.forEach(prison -> {
            if (whereaboutsEnabledService.isEnabled(prison.getAgencyId())) {
                log.info("cacheActivityLocations: Caching event locations for {}", prison.getAgencyId());

                final var now = LocalDate.now();
                List.of(TimeSlot.values()).forEach(slot -> {
                    log.info("cacheActivityLocations: Refreshing cache for {}, {}", prison.getAgencyId(), slot);
                    final var locations = agencyService.getAgencyEventLocationsBookedNonCached(prison.getAgencyId(), now, slot);
                    log.info("cacheActivityLocations: {} locations cached for {}, {}", locations.size(), prison.getAgencyId(), slot);
                });
            }
        });
        log.info("END: cacheActivityLocations, elapsed {} ms", System.currentTimeMillis() - start);
    }

}
