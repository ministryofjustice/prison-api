package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableList;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.Collections;
import java.util.List;

/**
 * Provides operations for locating offenders and other resources across the entire prison estate.
 */
@Service
@Validated
@Transactional(readOnly = true)
public class GlobalSearchService {
    public final static String DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT = "lastName,firstName,offenderNo";

    private static final ImmutableList<String> VALID_LOCATION_FILTER_VALUES = ImmutableList.of("ALL", "IN", "OUT");
    private static final ImmutableList<String> VALID_GENDER_FILTER_VALUES = ImmutableList.of("M", "F", "NK", "NS", "ALL");
    private final InmateRepository inmateRepository;
    private final PrisonerRepository prisonerRepository;

    public GlobalSearchService(final InmateRepository inmateRepository, final PrisonerRepository prisonerRepository) {
        this.inmateRepository = inmateRepository;
        this.prisonerRepository = prisonerRepository;
    }

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public Page<PrisonerDetail> findOffender(final String offenderNo, final PageRequest pageRequest) {
        val criteria = PrisonerDetailSearchCriteria.builder()
            .offenderNos(List.of(offenderNo))
            .build();
        return findOffenders(criteria, pageRequest);
    }

    public Page<PrisonerDetail> findOffenders(@NotNull @Valid final PrisonerDetailSearchCriteria criteria, final PageRequest pageRequest) {
        validateGenderFilter(criteria.getGender());
        validateLocationFilter(criteria.getLocation());

        final var adjustedPageRequest = pageRequest.withDefaultOrderBy(DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT);

        final Page<PrisonerDetail> prisonersPage;

        // Always force the use of streamlined SQL when searching by PNC or CRO for performance reasons
        if (criteria.isPrioritisedMatch() || StringUtils.isNotBlank(criteria.getPncNumber()) || StringUtils.isNotBlank(criteria.getCroNumber())) {
            prisonersPage = executePrioritisedQuery(criteria, adjustedPageRequest);
        } else {
            prisonersPage = executeQuery(criteria, adjustedPageRequest);
        }
        prisonersPage.getItems().forEach(p -> p.setLatestLocation(LocationProcessor.formatLocation(p.getLatestLocation())));

        return prisonersPage;
    }

    public Page<OffenderNumber> getOffenderNumbers(long offset, long limit) {
        return prisonerRepository.listAllOffenders(new PageRequest(offset, limit));
    }

    private Page<PrisonerDetail> executeQuery(final PrisonerDetailSearchCriteria criteria, final PageRequest pageRequest) {
        final var query = inmateRepository.generateFindOffendersQuery(criteria);

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

            response = prisonerRepository.findOffenders(criteria, pageRequest);

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

            response = prisonerRepository.findOffenders(criteria, pageRequest);

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
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("Location filter value %s not recognised.", location));
        }
    }

    private void validateGenderFilter(final String gender) {
        if (StringUtils.isNotBlank(gender) && !VALID_GENDER_FILTER_VALUES.contains(gender)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("Gender filter value %s not recognised.", gender));
        }
    }
}
