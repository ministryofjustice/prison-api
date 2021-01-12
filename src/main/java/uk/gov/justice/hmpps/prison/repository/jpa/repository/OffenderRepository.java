package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;

import java.time.LocalDate;
import java.util.List;

public interface OffenderRepository extends CrudRepository<Offender, Long> {
    List<Offender> findByNomsId(String nomsId);
    List<Offender> findByLastNameAndFirstNameAndBirthDate(final String lastName, final String firstName, final LocalDate dob);
}
