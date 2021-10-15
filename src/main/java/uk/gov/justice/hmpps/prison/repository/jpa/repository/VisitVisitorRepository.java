package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitVisitor;

import java.util.List;

public interface VisitVisitorRepository extends CrudRepository<VisitVisitor, Long> {
    List<VisitVisitor> findByVisitId(Long visitId);
}
