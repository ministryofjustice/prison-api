package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk;

@Repository
public interface ReferenceCodeRepository<T extends ReferenceCode> extends JpaRepository<T, Pk> {
}
