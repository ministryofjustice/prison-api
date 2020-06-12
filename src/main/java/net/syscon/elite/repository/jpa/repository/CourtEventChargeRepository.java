package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.CourtEventCharge;
import org.springframework.data.repository.CrudRepository;

public interface CourtEventChargeRepository extends CrudRepository<CourtEventCharge, CourtEventCharge.Pk> {
}
