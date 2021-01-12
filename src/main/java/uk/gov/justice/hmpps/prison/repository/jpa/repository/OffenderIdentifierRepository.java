package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier.OffenderIdentifierPK;

import java.util.List;

@Repository
public interface OffenderIdentifierRepository extends CrudRepository<OffenderIdentifier, OffenderIdentifierPK> {

    List<OffenderIdentifier> findByIdentifierTypeAndIdentifier(final String type, final String id);
}
