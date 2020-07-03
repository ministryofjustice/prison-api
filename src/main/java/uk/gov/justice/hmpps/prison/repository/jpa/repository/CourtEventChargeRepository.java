package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge;

public interface CourtEventChargeRepository extends CrudRepository<CourtEventCharge, CourtEventCharge.Pk> {
}
