package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge;

import java.util.List;

public interface CourtEventChargeRepository extends CrudRepository<CourtEventCharge, CourtEventCharge.Pk> {

    @Query("select " +
        "cec " +
        "from CourtEventCharge cec " +
        "join cec.eventAndCharge.offenderCharge oc " +
        "join oc.offenderBooking ob " +
        "join ob.offender o " +
        "where o.nomsId = :offenderId"
    )
    List<CourtEventCharge> findByOffender(String offenderId);
}
