package net.syscon.prison.repository.jpa.repository;


import net.syscon.prison.repository.jpa.model.PrisonerStatusInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrisonerStatusInformationRepository extends PagingAndSortingRepository<PrisonerStatusInformation, String> {

    Optional<PrisonerStatusInformation> getByNomsId(final String nomsId);
    Page<PrisonerStatusInformation> findAllByEstablishmentCode(final String establishmentCode, Pageable pageable);
}
