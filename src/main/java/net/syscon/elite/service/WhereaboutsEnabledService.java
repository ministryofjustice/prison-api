package net.syscon.elite.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WhereaboutsEnabledService {

    private final LocationGroupService service;
    private final Set<String> enabledAgencies;

    public WhereaboutsEnabledService(
            @Qualifier("overrideLocationGroupService") LocationGroupService service,
            @Qualifier("whereaboutsEnabled") Set<String> enabledAgencies) {
        this.service = service;
        this.enabledAgencies = enabledAgencies;
    }

    public boolean isEnabled(String agencyId) {
        return !service.getLocationGroups(agencyId).isEmpty() || enabledAgencies.contains(agencyId);
    }
}
