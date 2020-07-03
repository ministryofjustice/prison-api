package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.Offender;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderRepository extends CrudRepository<Offender, Long> {
    List<Offender> findByNomsId(String nomsId);
}
