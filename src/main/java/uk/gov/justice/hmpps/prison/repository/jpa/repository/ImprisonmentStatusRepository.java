package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus;

import java.util.Optional;

public interface ImprisonmentStatusRepository extends CrudRepository<ImprisonmentStatus, Long> {
    Optional<ImprisonmentStatus> findByStatusAndActiveFlag(String status, String activeFlag);
}
