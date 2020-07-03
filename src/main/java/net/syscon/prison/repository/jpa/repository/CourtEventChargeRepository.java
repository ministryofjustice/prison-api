package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.CourtEventCharge;
import org.springframework.data.repository.CrudRepository;

public interface CourtEventChargeRepository extends CrudRepository<CourtEventCharge, CourtEventCharge.Pk> {
}
