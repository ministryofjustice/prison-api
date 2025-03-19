package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderLanguage;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffenderLanguageRepository extends CrudRepository<OffenderLanguage, OffenderLanguage.PK> {
    List<OffenderLanguage> findByOffenderBookId(Long bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ol from OffenderLanguage ol where ol.offenderBookId = :bookingId")
    List<OffenderLanguage> findByOffenderBookIdForUpdate(Long bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ol from OffenderLanguage ol where ol.offenderBookId = :bookingId and ol.code = :code and ol.type = :type")
    Optional<OffenderLanguage> findByOffenderBookIdAndCodeAndTypeForUpdate(Long bookingId, String code, String type);
}
