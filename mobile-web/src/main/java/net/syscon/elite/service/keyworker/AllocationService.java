package net.syscon.elite.service.keyworker;

import net.syscon.elite.service.AllocationException;

public interface AllocationService {
    String ALLOCATION_REASON_AUTO = "AUTO";
    String ALLOCATION_REASON_MANUAL = "MANUAL";

    void autoAllocate(String agencyId) throws AllocationException;
}
