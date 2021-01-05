package uk.gov.justice.hmpps.prison.repository.jpa.repository;


import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail.PK;

import java.util.List;

public interface OffenderProfileDetailRepository extends CrudRepository<OffenderProfileDetail, PK> {

    List<OffenderProfileDetail> findAllByBookingIdAndType(final Long bookingId, final String type);

}
