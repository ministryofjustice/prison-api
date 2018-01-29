package net.syscon.elite.service.keyworker;

import net.syscon.elite.service.AllocationException;

public interface AllocationService {
    String OUTCOME_NO_UNALLOCATED_OFFENDERS = "No unallocated offenders.";
    String OUTCOME_NO_AVAILABLE_KEY_WORKERS = "No Key workers available for allocation.";
    String OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY = "All available Key workers are at full capacity.";

    void autoAllocate(String agencyId) throws AllocationException;
}
