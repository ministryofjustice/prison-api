package net.syscon.elite.service.impl.whereabouts;

import net.syscon.elite.service.LocationGroupService;
import net.syscon.elite.service.WhereaboutsEnabledService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WhereaboutsEnabledServiceImpl implements WhereaboutsEnabledService {

    private final LocationGroupService service;
    private final Set<String> enabledAgencies;

    public WhereaboutsEnabledServiceImpl(
            @Qualifier("overrideLocationGroupService") LocationGroupService service,
            @Qualifier("whereaboutsEnabled") Set<String> enabledAgencies) {
        this.service = service;
        this.enabledAgencies = enabledAgencies;
    }

    @Override
    public boolean isEnabled(String agencyId) {
        return !service.getLocationGroups(agencyId).isEmpty() || enabledAgencies.contains(agencyId);
    }
}
