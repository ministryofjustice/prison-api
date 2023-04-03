package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge;

import java.util.List;

public interface CourtEventChargeRepository extends CrudRepository<CourtEventCharge, CourtEventCharge.Pk> {

    /*
        Find all court event charges for a given offender.
        This also filters out data where there is missing court event data, caused by a
        NOMIS bug where orphan records were left when deleting court data.
     */
    @Query("select " +
        "cec " +
        "from CourtEventCharge cec " +
        "join cec.eventAndCharge.offenderCharge oc " +
        "join oc.offenderBooking ob " +
        "join ob.offender o " +
        "where o.nomsId = :offenderId " +
        "and exists (select id from CourtEvent where id = cec.eventAndCharge.courtEvent.id)"
    )
    List<CourtEventCharge> findByOffender(String offenderId);
}
