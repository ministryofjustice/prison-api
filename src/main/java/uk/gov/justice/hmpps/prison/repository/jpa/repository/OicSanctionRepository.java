package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction;

import java.util.List;

public interface OicSanctionRepository extends JpaRepository<OicSanction, PK> {

    List<OicSanction> findByOicHearingId(Long oicHearingId);

    @Query(value = "SELECT NVL(MAX(SANCTION_SEQ)+1, 1) FROM OFFENDER_OIC_SANCTIONS oos WHERE OFFENDER_BOOK_ID = :offenderBookId", nativeQuery = true)
    Long getNextSanctionSeq(Long offenderBookId);
}
