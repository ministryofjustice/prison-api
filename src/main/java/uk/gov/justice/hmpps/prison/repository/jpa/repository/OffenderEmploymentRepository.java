package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEmployment;

public interface OffenderEmploymentRepository extends PagingAndSortingRepository<OffenderEmployment, OffenderEmployment.PK> {

    Page<OffenderEmployment> findAllByNomisId(@Param("nomsId") String nomsId, Pageable pageable);

}