package net.syscon.elite.service.keyworker;

import net.syscon.elite.service.AllocationException;

public interface KeyworkerAutoAllocationService {
    String ALLOCATION_REASON_AUTO = "AUTO";
    String ALLOCATION_REASON_MANUAL = "MANUAL";

    Long autoAllocate(String agencyId) throws AllocationException;
}
