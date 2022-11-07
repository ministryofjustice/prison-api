package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus;

import java.util.Optional;

public interface ImprisonmentStatusRepository extends CrudRepository<ImprisonmentStatus, Long> {
    String ADULT_IMPRISONMENT_WITHOUT_OPTION = "SENT03";
    Optional<ImprisonmentStatus> findByStatusAndActive(String status, boolean active);
}
