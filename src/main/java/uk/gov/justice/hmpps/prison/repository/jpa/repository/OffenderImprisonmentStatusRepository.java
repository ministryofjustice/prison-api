package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;

import java.util.List;

@Repository
public interface OffenderImprisonmentStatusRepository extends CrudRepository<OffenderImprisonmentStatus, OffenderImprisonmentStatus.PK> {
    List<OffenderImprisonmentStatus> findByOffenderBookId(final long offenderBookId);
}
