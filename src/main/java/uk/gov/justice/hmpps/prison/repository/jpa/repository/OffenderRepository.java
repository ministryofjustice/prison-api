package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;

import java.util.List;

public interface OffenderRepository extends CrudRepository<Offender, Long> {
    List<Offender> findByNomsId(String nomsId);
}
