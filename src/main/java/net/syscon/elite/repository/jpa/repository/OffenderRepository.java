package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Offender;
import org.springframework.data.repository.CrudRepository;

public interface OffenderRepository extends CrudRepository<Offender, Long> {
    Offender findByNomsId(String nomsId);
}
