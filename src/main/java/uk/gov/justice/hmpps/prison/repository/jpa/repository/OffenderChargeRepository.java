package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;

import java.util.List;
import java.util.Set;

public interface OffenderChargeRepository extends CrudRepository<OffenderCharge, Long> {
    @EntityGraph(type = EntityGraphType.FETCH, value = "charges-details")
    List<OffenderCharge> findByOffenderBooking_BookingIdInAndChargeStatusAndOffenderCourtCase_CaseStatus_Code(Set<Long> bookingIds, String chargeStatus, String caseStatusCode);
}
