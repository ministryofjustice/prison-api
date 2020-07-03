package uk.gov.justice.hmpps.nomis.prison.datacompliance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;

import java.util.List;

@Repository
public interface OffenderAliasPendingDeletionRepository extends CrudRepository<OffenderAliasPendingDeletion, String> {

    List<OffenderAliasPendingDeletion> findOffenderAliasPendingDeletionByOffenderNumber(@Param("offenderNumber") final String offenderNumber);
}
