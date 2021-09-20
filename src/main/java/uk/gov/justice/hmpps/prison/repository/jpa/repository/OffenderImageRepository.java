package uk.gov.justice.hmpps.prison.repository.jpa.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffenderImageRepository extends CrudRepository<OffenderImage, Long> {

    @Query(value = "SELECT i FROM OffenderImage i join i.offenderBooking ob join ob.offender o where o.nomsId = :offenderNumber")
    List<OffenderImage> getImagesByOffenderNumber(@Param("offenderNumber") final String offenderNumber);

    @Query(value = "select oi from OffenderImage oi where oi.id = (SELECT max(i.id) FROM OffenderImage i join i.offenderBooking ob join ob.offender o where o.nomsId = :offenderNumber " +
        "and i.viewType = 'FACE' and i.orientationType = 'FRONT' and i.imageType = 'OFF_BKG' and i.activeFlag = 'Y')")
    Optional<OffenderImage> findLatestByOffenderNumber(@Param("offenderNumber") final String offenderNumber);

    @Query(value = "select oi from OffenderImage oi where oi.id = (SELECT max(i.id) FROM OffenderImage i where i.offenderBooking.bookingId = :bookingId " +
        "and i.viewType = 'FACE' and i.orientationType = 'FRONT' and i.imageType = 'OFF_BKG' )")
    Optional<OffenderImage> findLatestByBookingId(@Param("bookingId") final Long bookingId);
}
