package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.model.PrisonerDetailSearchCriteria;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import java.util.Collections;

/**
 * Implementation of global search service.
 */
@Service
@Validated
@Transactional(readOnly = true)
public class GlobalSearchServiceImpl implements GlobalSearchService {
    private static final ImmutableList<String> VALID_LOCATION_FILTER_VALUES = ImmutableList.of("ALL", "IN", "OUT");
    private static final ImmutableList<String> VALID_GENDER_FILTER_VALUES = ImmutableList.of("M", "F", "NK", "NS", "ALL");
    private final InmateRepository inmateRepository;
    private final OffenderRepository offenderRepository;

    public GlobalSearchServiceImpl(final InmateRepository inmateRepository, final OffenderRepository offenderRepository) {
        this.inmateRepository = inmateRepository;
        this.offenderRepository = offenderRepository;
    }

    @Override
    public Page<PrisonerDetail> findOffenders(@NotNull @Valid final PrisonerDetailSearchCriteria criteria, final PageRequest pageRequest) {
        validateGenderFilter(criteria.getGender());
        validateLocationFilter(criteria.getLocation());

        final var adjustedPageRequest = pageRequest.withDefaultOrderBy(DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT);

        final Page<PrisonerDetail> prisonersPage;

        try {
            // Always force the use of streamlined SQL when searching by PNC or CRO for performance reasons
            if (criteria.isPrioritisedMatch() || StringUtils.isNotBlank(criteria.getPncNumber()) || StringUtils.isNotBlank(criteria.getCroNumber())) {
                prisonersPage = executePrioritisedQuery(criteria, adjustedPageRequest);
            } else {
                prisonersPage = executeQuery(criteria, adjustedPageRequest);
            }
            prisonersPage.getItems().forEach(p -> p.setLatestLocation(LocationProcessor.formatLocation(p.getLatestLocation())));
        } catch (final InvalidDataAccessApiUsageException iaex) {
            throw new BadRequestException(iaex.getMostSpecificCause().getMessage(), iaex);
        }

        return prisonersPage;
    }

    private Page<PrisonerDetail> executeQuery(final PrisonerDetailSearchCriteria criteria, final PageRequest pageRequest) {
        final var query = InmateRepository.generateFindOffendersQuery(criteria);

        if (StringUtils.isNotBlank(query)) {
            return criteria.isIncludeAliases() ? inmateRepository.findOffendersWithAliases(query, pageRequest) : inmateRepository.findOffenders(query, pageRequest);
        }

        return new Page<>(Collections.emptyList(), 0, pageRequest.getOffset(), pageRequest.getLimit());
    }

    private Page<PrisonerDetail> executePrioritisedQuery(final PrisonerDetailSearchCriteria criteria, final PageRequest pageRequest) {
        return executeOffenderNoQuery(criteria, pageRequest);
    }

    private Page<PrisonerDetail> executeOffenderNoQuery(final PrisonerDetailSearchCriteria originalCriteria, final PageRequest pageRequest) {
        Page<PrisonerDetail> response;

        final var offenderNoCriteria = originalCriteria.getOffenderNos();

        if (offenderNoCriteria != null && !offenderNoCriteria.isEmpty()) {
            final var criteria = PrisonerDetailSearchCriteria.builder()
                    .offenderNos(offenderNoCriteria).build();

            response = executeQuery(criteria, pageRequest);

            if (response.getItems().isEmpty()) {
                response = executePncNumberQuery(originalCriteria, pageRequest);
            }
        } else {
            response = executePncNumberQuery(originalCriteria, pageRequest);
        }

        return response;
    }

    private Page<PrisonerDetail> executePncNumberQuery(final PrisonerDetailSearchCriteria originalCriteria, final PageRequest pageRequest) {
        Page<PrisonerDetail> response;

        final var pncNumberCriteria = originalCriteria.getPncNumber();

        if (StringUtils.isNotBlank(pncNumberCriteria)) {
            final var criteria = PrisonerDetailSearchCriteria.builder()
                    .pncNumber(pncNumberCriteria).build();

            response = offenderRepository.findOffenders(criteria, pageRequest);

            if (response.getItems().isEmpty()) {
                response = executeCroNumberQuery(originalCriteria, pageRequest);
            }
        } else {
            response = executeCroNumberQuery(originalCriteria, pageRequest);
        }

        return response;
    }

    private Page<PrisonerDetail> executeCroNumberQuery(final PrisonerDetailSearchCriteria originalCriteria, final PageRequest pageRequest) {
        Page<PrisonerDetail> response;

        final var croNumberCriteria = originalCriteria.getCroNumber();

        if (StringUtils.isNotBlank(croNumberCriteria)) {
            final var criteria = PrisonerDetailSearchCriteria.builder()
                    .croNumber(croNumberCriteria).build();

            response = offenderRepository.findOffenders(criteria, pageRequest);

            if (response.getItems().isEmpty()) {
                response = executePersonalAttrsQuery(originalCriteria, pageRequest);
            }
        } else {
            response = executePersonalAttrsQuery(originalCriteria, pageRequest);
        }

        return response;
    }

    private Page<PrisonerDetail> executePersonalAttrsQuery(final PrisonerDetailSearchCriteria originalCriteria, final PageRequest pageRequest) {
        final var criteria = PrisonerDetailSearchCriteria.builder()
                .lastName(originalCriteria.getLastName())
                .firstName(originalCriteria.getFirstName())
                .dob(originalCriteria.getDob())
                .partialNameMatch(originalCriteria.isPartialNameMatch())
                .anyMatch(originalCriteria.isAnyMatch())
                .build();

        var response = executeQuery(criteria, pageRequest);

        if (response.getItems().isEmpty()) {
            response = executeDobRangeQuery(originalCriteria, pageRequest);
        }

        return response;
    }

    private Page<PrisonerDetail> executeDobRangeQuery(final PrisonerDetailSearchCriteria originalCriteria, final PageRequest pageRequest) {
        final var criteria = PrisonerDetailSearchCriteria.builder()
                .dobFrom(originalCriteria.getDobFrom())
                .dobTo(originalCriteria.getDobTo())
                .maxYearsRange(originalCriteria.getMaxYearsRange())
                .build();

        return executeQuery(criteria, pageRequest);
    }


    private void validateLocationFilter(final String location) {
        if (StringUtils.isNotBlank(location) && !VALID_LOCATION_FILTER_VALUES.contains(location)) {
            throw new BadRequestException(String.format("Location filter value %s not recognised.", location));
        }
    }

    private void validateGenderFilter(final String gender) {
        if (StringUtils.isNotBlank(gender) && !VALID_GENDER_FILTER_VALUES.contains(gender)) {
            throw new BadRequestException(String.format("Gender filter value %s not recognised.", gender));
        }
    }
}
