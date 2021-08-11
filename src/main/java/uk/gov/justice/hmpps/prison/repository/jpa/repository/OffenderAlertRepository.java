package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert;

import java.util.List;

public interface OffenderAlertRepository extends PagingAndSortingRepository<OffenderAlert, OffenderAlert.PK>, JpaSpecificationExecutor<OffenderAlert> {
    List<OffenderAlert> findAllByOffenderBooking_BookingId(final Long bookingId);
}
