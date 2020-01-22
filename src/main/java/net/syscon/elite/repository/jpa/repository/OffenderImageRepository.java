package net.syscon.elite.repository.jpa.repository;


import net.syscon.elite.repository.jpa.model.OffenderImage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffenderImageRepository extends CrudRepository<OffenderImage, Long> {

    @Query(value =
            "SELECT * FROM offender_images i " +
            "INNER JOIN offender_bookings ob " +
            "ON ob.offender_book_id = i.offender_book_id " +
            "INNER JOIN offenders o " +
            "ON o.offender_id = ob.offender_id " +
            "WHERE o.offender_id_display = :offenderNo",
            nativeQuery = true)
    List<OffenderImage> getImagesByOffenderNumber(@Param("offenderNo") final String offenderNumber);
}
