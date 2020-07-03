package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderLanguage;

import java.util.List;

@Repository
public interface OffenderLanguageRepository extends CrudRepository<OffenderLanguage, OffenderLanguage.PK> {
    List<OffenderLanguage> findByOffenderBookId(Long bookingId);
}
