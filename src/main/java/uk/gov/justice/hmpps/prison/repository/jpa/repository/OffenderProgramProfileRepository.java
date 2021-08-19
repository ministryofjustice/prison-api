package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;

import java.util.List;

public interface OffenderProgramProfileRepository extends CrudRepository<OffenderProgramProfile, Long> {
    List<OffenderProgramProfile> findByOffenderBooking_BookingIdAndProgramStatus(Long bookingId, String programStatus);
}
