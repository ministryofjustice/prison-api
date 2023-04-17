package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction;

import java.util.List;

public interface OicSanctionRepository extends CrudRepository<OicSanction, PK> {

    List<OicSanction> findAllByOicHearingId(Long oicHearingId);
}
