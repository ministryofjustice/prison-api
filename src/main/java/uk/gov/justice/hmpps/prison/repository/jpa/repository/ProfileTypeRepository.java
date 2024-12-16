package uk.gov.justice.hmpps.prison.repository.jpa.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType;

import java.util.Optional;

@Repository
public interface ProfileTypeRepository extends CrudRepository<ProfileType, String> {

    Optional<ProfileType> findByTypeAndCategoryAndActive(String type, String category, boolean active);
}
