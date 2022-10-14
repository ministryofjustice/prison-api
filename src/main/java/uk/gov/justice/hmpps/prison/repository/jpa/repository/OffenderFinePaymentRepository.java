package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFinePayment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence.PK;

import java.util.List;

public interface OffenderFinePaymentRepository extends CrudRepository<OffenderFinePayment, PK> {
    List<OffenderFinePayment> findByOffenderBooking_BookingId(Long bookingId);
}
