package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier.OffenderIdentifierPK;

import java.util.List;

@Repository
public interface OffenderIdentifierRepository extends CrudRepository<OffenderIdentifier, OffenderIdentifierPK> {

    @Modifying
    @Query("""
        update OffenderIdentifier oi
        set oi.offenderIdentifierPK.offenderId = :newOffenderId
        where oi.offenderIdentifierPK.offenderId = :oldOffenderId
    """)
    void moveIdentifiersToNewAlias(final long oldOffenderId, final long newOffenderId);

    List<OffenderIdentifier> findOffenderIdentifiersByOffender_NomsId(final String NomsId);

    List<OffenderIdentifier> findByIdentifierTypeAndIdentifier(final String type, final String id);
}
