package net.syscon.elite.service;

import net.syscon.elite.web.api.model.InmatesSummary;

import java.util.List;

/**
 * AssignmentService
 */
public interface AssignmentService {
    List<InmatesSummary> findMyAssignments(int offset, int limit);
}
