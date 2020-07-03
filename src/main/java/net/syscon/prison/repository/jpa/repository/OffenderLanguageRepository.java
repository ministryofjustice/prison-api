package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.OffenderLanguage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffenderLanguageRepository extends CrudRepository<OffenderLanguage, OffenderLanguage.PK> {
    List<OffenderLanguage> findByOffenderBookId(Long bookingId);
}
