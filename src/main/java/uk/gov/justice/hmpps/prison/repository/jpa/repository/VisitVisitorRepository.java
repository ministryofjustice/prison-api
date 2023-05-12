package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitVisitor;

import java.util.List;

public interface VisitVisitorRepository extends CrudRepository<VisitVisitor, Long> {
    @EntityGraph(type = EntityGraphType.FETCH, value = "visitor-with-person")
    List<VisitVisitor> findByVisitIdInAndOffenderBookingIsNullOrderByPerson_BirthDateDesc(List<Long> visitIds);
}
