package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.gov.justice.hmpps.prison.api.model.OffenderTeamAssignmentResponse;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment.PK;

public interface OffenderTeamAssignmentRepository extends
    PagingAndSortingRepository<OffenderTeamAssignment, OffenderTeamAssignment.PK>,
    CrudRepository<OffenderTeamAssignment, PK> {

    @Query(
        value = """
            SELECT new uk.gov.justice.hmpps.prison.api.model.OffenderTeamAssignmentResponse(
                a.id.offenderBooking.bookingId, a.id.functionTypeCode, a.team.id, a.assignmentDate
            )
            FROM OffenderTeamAssignment a
            ORDER BY a.id.offenderBooking.bookingId, a.id.functionTypeCode
            """,
        countQuery = "SELECT COUNT(a) FROM OffenderTeamAssignment a"
    )
    Page<OffenderTeamAssignmentResponse> findAssignments(Pageable pageable);
}
