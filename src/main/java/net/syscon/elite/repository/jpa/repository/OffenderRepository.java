package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Offender;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderRepository extends CrudRepository<Offender, Long> {
    List<Offender> findByNomsId(String nomsId);
}
