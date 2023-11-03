package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPayStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPayStatus.PK;

import java.util.List;

public interface OffenderPayStatusRepository extends CrudRepository<OffenderPayStatus, PK> {

    List<OffenderPayStatus> findAllByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);
}
