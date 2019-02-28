package net.syscon.elite.scheduling;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.LocationGroupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class ActivityCacheScheduler {

    private final AgencyService agencyService;
    private final LocationGroupService locationGroupService;

    public ActivityCacheScheduler(AgencyService agencyService, LocationGroupService locationGroupService) {
        this.agencyService = agencyService;
        this.locationGroupService = locationGroupService;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000, initialDelay = 30000)
    public void cacheActivityLocations() {

        final long start = System.currentTimeMillis();
        log.info("START: cacheActivityLocations");
        // get all prisons
        var prisons = agencyService.getAgenciesByType("INST");

        prisons.forEach(prison -> {
            final var locationGroups = locationGroupService.getLocationGroups(prison.getAgencyId());

            if (!locationGroups.isEmpty()) {
                log.info("cacheActivityLocations: Caching event locations for {}", prison.getAgencyId());

                final var now = LocalDate.now();
                List.of(TimeSlot.values()).forEach(slot -> {
                    log.info("cacheActivityLocations: Refreshing cache for {}, {}", prison.getAgencyId(), slot);
                    var locations = agencyService.getAgencyEventLocationsBookedNonCached(prison.getAgencyId(), now, slot);
                    log.info("cacheActivityLocations: {} locations cached for {}, {}", locations.size(), prison.getAgencyId(), slot);
                });
            }
        });
        log.info("END: cacheActivityLocations, elapsed {} ms", System.currentTimeMillis() - start);
    }

}
