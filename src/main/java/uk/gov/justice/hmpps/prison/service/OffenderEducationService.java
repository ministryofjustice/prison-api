package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Education;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderEducationRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEducationTransformer;

import jakarta.validation.constraints.NotNull;

@Service
@Transactional(readOnly = true)
public class OffenderEducationService {

    private final OffenderEducationRepository repository;
    private final OffenderEducationTransformer transformer;
    private final int batchSize;

    public OffenderEducationService(
        final OffenderEducationRepository repository,
        final OffenderEducationTransformer transformer,
        @Value("${batch.max.size:1000}") final int batchSize
    ) {
        this.repository = repository;
        this.transformer = transformer;
        this.batchSize = batchSize;
    }

    @VerifyOffenderAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Page<Education> getOffenderEducations(@NotNull final String nomisId, final PageRequest pageRequest) {
        return repository.findAllByNomisId(nomisId, pageRequest).map(transformer::convert);
    }

    @VerifyOffenderAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<Education> getOffenderEducations(@NotNull final List<String> nomisIds) {
        return Lists.partition(nomisIds, batchSize)
            .stream()
            .flatMap(noms -> repository.findAllByNomisIdIn(noms).stream())
            .map(transformer::convert)
            .collect(Collectors.toList());
    }
}
