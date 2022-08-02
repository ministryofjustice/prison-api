package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderHealthProblem;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OffenderHealthProblemRepository extends CrudRepository<OffenderHealthProblem, Long> {

    List<OffenderHealthProblem> findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateAfterAndStartDateBefore(List<String> nomisId, Integer bookingSequence, String problemTypeCode, LocalDate fromStartDate, LocalDate toStartDate);


}
