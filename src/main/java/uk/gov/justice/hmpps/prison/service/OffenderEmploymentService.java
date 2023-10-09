package uk.gov.justice.hmpps.prison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Employment;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderEmploymentRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEmploymentTransformer;

import jakarta.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderEmploymentService {

    private final OffenderEmploymentRepository repository;
    private final OffenderEmploymentTransformer transformer;

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Page<Employment> getOffenderEmployments(@NotNull final String nomisId, final PageRequest pageRequest) {
        return repository.findAllByNomisId(nomisId, pageRequest).map(transformer::convert);
    }
}
