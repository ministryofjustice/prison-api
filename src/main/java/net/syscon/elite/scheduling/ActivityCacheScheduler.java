package net.syscon.elite.scheduling;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.LocationGroupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

        // get all prisons
        var prisons = agencyService.getAgenciesByType("INST");

        prisons.forEach(prison -> {
            var locationGroups = locationGroupService.getLocationGroups(prison.getAgencyId());

            if (!locationGroups.isEmpty()) {
                log.info("Caching event locations for {}", prison.getAgencyId());

                agencyService.evictAgencyEventLocationsBooked(prison.getAgencyId(), LocalDate.now(), TimeSlot.AM);
                agencyService.getAgencyEventLocationsBooked(prison.getAgencyId(), LocalDate.now(), TimeSlot.AM);

                agencyService.evictAgencyEventLocationsBooked(prison.getAgencyId(), LocalDate.now(), TimeSlot.PM);
                agencyService.getAgencyEventLocationsBooked(prison.getAgencyId(), LocalDate.now(), TimeSlot.PM);

                agencyService.evictAgencyEventLocationsBooked(prison.getAgencyId(), LocalDate.now(), TimeSlot.ED);
                agencyService.getAgencyEventLocationsBooked(prison.getAgencyId(), LocalDate.now(), TimeSlot.ED);
            }
        });
    }

}
