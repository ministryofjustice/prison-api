package uk.gov.justice.hmpps.prison.repository.jpa.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType;

import java.util.List;

@Repository
public interface ProfileCodeRepository extends CrudRepository<ProfileCode, ProfileCode.PK> {

    @Query("select pc from ProfileCode pc where pc.id.type = :type")
    List<ProfileCode> findByProfileType(ProfileType type);
}
