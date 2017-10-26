package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Page;

/**
 * AssignmentService
 */
public interface AssignmentService {
    Page<OffenderBooking> findMyAssignments(long offset, long limit);
}
