package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment;

public interface OffenderTeamAssignmentRepository extends PagingAndSortingRepository<OffenderTeamAssignment, OffenderTeamAssignment.PK> {
}
