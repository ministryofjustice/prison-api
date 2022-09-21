package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceIndicator;

public interface OffenceIndicatorRepository extends CrudRepository<OffenceIndicator, Long> {
    Long deleteByIndicatorCodeAndOffence_Code(String indicatorCode, String offenceCode);
}
