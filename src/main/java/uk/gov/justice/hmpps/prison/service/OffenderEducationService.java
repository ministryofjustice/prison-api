package uk.gov.justice.hmpps.prison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Education;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderEducationRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEducationTransformer;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderEducationService {

    private final OffenderEducationRepository repository;
    private final OffenderEducationTransformer transformer;

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Page<Education> getOffenderEducations(@NotNull final String nomisId, final PageRequest pageRequest) {
        return repository.findAllByNomisId(nomisId, pageRequest).map(transformer::convert);
    }
}
