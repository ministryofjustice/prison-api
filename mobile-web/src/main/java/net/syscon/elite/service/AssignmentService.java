package net.syscon.elite.service;

import net.syscon.elite.web.api.model.InmateAssignmentSummary;

import java.util.List;

/**
 * AssignmentService
 */
public interface AssignmentService {
    List<InmateAssignmentSummary> findMyAssignments(int offset, int limit);
}
