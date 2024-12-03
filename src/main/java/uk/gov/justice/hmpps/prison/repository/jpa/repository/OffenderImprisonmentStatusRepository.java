package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus.PK;

import java.util.List;

public interface OffenderImprisonmentStatusRepository  extends CrudRepository<OffenderImprisonmentStatus, PK> {

    @Query("""
        select ois
            from OffenderImprisonmentStatus ois
            join       ois.offenderBooking ob
            join       ob.offender o
            join fetch ois.imprisonmentStatus is
            where o.nomsId = :offenderId
        """
    )
    List<OffenderImprisonmentStatus> findByOffender(String offenderId);
}
