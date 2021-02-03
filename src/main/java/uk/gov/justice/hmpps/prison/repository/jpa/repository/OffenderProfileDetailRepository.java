package uk.gov.justice.hmpps.prison.repository.jpa.repository;


import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail.PK;

public interface OffenderProfileDetailRepository extends CrudRepository<OffenderProfileDetail, PK> {

}
