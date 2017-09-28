package net.syscon.elite.service;

import net.syscon.elite.v2.api.model.OffenderBooking;

import java.util.List;

/**
 * AssignmentService
 */
public interface AssignmentService {
    List<OffenderBooking> findMyAssignments(long offset, long limit);
}
