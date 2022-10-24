package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult;

import java.util.Optional;

public interface OffenceResultRepository extends CrudRepository<OffenceResult, String> {
    String IMPRISONMENT = "1002";
    String NOT_GUILTY = "2004";
}
