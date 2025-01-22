package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PrisonerStatusInformation;

import java.util.Optional;

@Repository
public interface PrisonerStatusInformationRepository extends PagingAndSortingRepository<PrisonerStatusInformation, String> {

    Optional<PrisonerStatusInformation> getByNomsId(final String nomsId);
}
