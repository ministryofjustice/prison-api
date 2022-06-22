package uk.gov.justice.hmpps.prison.repository.jpa.repository;


import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType;

import java.util.List;

public interface OffenderProfileDetailRepository extends CrudRepository<OffenderProfileDetail, PK> {

    List<OffenderProfileDetail> findByOffenderBookingBookingIdAndTypeAndSequence(Long bookingId, ProfileType type, Integer sequence);
}
