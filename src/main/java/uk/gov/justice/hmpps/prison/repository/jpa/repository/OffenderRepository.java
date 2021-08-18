package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OffenderRepository extends CrudRepository<Offender, Long> {
    List<Offender> findByNomsId(String nomsId);
    List<Offender> findByLastNameAndFirstNameAndBirthDate(final String lastName, final String firstName, final LocalDate dob);

    @Query("select o from Offender o left join fetch o.bookings b WHERE o.nomsId = :nomsId order by b.bookingSequence asc")
    List<Offender> findOffendersByNomsId(@Param("nomsId") String nomsId, Pageable pageable);

    default Optional<Offender> findOffenderByNomsId(String nomsId){
        return findOffendersByNomsId(nomsId, PageRequest.of(0,1)).stream().findFirst();
    };
}
