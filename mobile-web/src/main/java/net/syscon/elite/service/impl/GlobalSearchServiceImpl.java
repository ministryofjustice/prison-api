package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.BadRequestException;
import java.util.Collections;

/**
 * Implementation of global search service.
 */
@Service
@Transactional(readOnly = true)
public class GlobalSearchServiceImpl implements GlobalSearchService {
    private final InmateRepository inmateRepository;
    private final OffenderRepository offenderRepository;

    @Value("${offender.dob.max.range.years:10}")
    private int maxYears;

    public GlobalSearchServiceImpl(InmateRepository inmateRepository, OffenderRepository offenderRepository) {
        this.inmateRepository = inmateRepository;
        this.offenderRepository = offenderRepository;
    }

    @Override
    public Page<PrisonerDetail> findOffenders(PrisonerDetailSearchCriteria criteria, PageRequest pageRequest) {
        PrisonerDetailSearchCriteria decoratedCriteria = criteria.withMaxYearsRange(maxYears);
        PageRequest adjustedPageRequest = pageRequest.withDefaultOrderBy(DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT);

        Page<PrisonerDetail> response;

        try {
            if (decoratedCriteria.isPrioritisedMatch()) {
                response = executePrioritisedQuery(decoratedCriteria, adjustedPageRequest);
            } else {
                response = executeQuery(decoratedCriteria, adjustedPageRequest);
            }
        } catch (IllegalArgumentException iaex) {
            throw new BadRequestException("Invalid search criteria.", iaex);
        }

        return response;
    }

    private Page<PrisonerDetail> executeQuery(PrisonerDetailSearchCriteria criteria, PageRequest pageRequest) {
        String query = InmateRepository.generateFindOffendersQuery(criteria);

        if (StringUtils.isNotBlank(query)) {
            return inmateRepository.findOffenders(query, pageRequest);
        }

        return new Page<>(Collections.emptyList(), 0, pageRequest.getOffset(), pageRequest.getLimit());
    }

    private Page<PrisonerDetail> executePrioritisedQuery(PrisonerDetailSearchCriteria criteria, PageRequest pageRequest) {
        return executeOffenderNoQuery(criteria, pageRequest);
    }

    private Page<PrisonerDetail> executeOffenderNoQuery(PrisonerDetailSearchCriteria originalCriteria, PageRequest pageRequest) {
        Page<PrisonerDetail> response;

        String offenderNoCriteria = originalCriteria.getOffenderNo();

        if (StringUtils.isNotBlank(offenderNoCriteria)) {
            PrisonerDetailSearchCriteria criteria = PrisonerDetailSearchCriteria.builder()
                    .offenderNo(offenderNoCriteria).build();

            response = executeQuery(criteria, pageRequest);

            if (response.getItems().isEmpty()) {
                response = executePncNumberQuery(originalCriteria, pageRequest);
            }
        } else {
            response = executePncNumberQuery(originalCriteria, pageRequest);
        }

        return response;
    }

    private Page<PrisonerDetail> executePncNumberQuery(PrisonerDetailSearchCriteria originalCriteria, PageRequest pageRequest) {
        Page<PrisonerDetail> response;

        String pncNumberCriteria = originalCriteria.getPncNumber();

        if (StringUtils.isNotBlank(pncNumberCriteria)) {
            PrisonerDetailSearchCriteria criteria = PrisonerDetailSearchCriteria.builder()
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

    private Page<PrisonerDetail> executeCroNumberQuery(PrisonerDetailSearchCriteria originalCriteria, PageRequest pageRequest) {
        Page<PrisonerDetail> response;

        String croNumberCriteria = originalCriteria.getCroNumber();

        if (StringUtils.isNotBlank(croNumberCriteria)) {
            PrisonerDetailSearchCriteria criteria = PrisonerDetailSearchCriteria.builder()
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

    private Page<PrisonerDetail> executePersonalAttrsQuery(PrisonerDetailSearchCriteria originalCriteria, PageRequest pageRequest) {
        PrisonerDetailSearchCriteria criteria =  PrisonerDetailSearchCriteria.builder()
                .lastName(originalCriteria.getLastName())
                .firstName(originalCriteria.getFirstName())
                .dob(originalCriteria.getDob())
                .partialNameMatch(originalCriteria.isPartialNameMatch())
                .anyMatch(originalCriteria.isAnyMatch())
                .build();

        Page<PrisonerDetail> response = executeQuery(criteria, pageRequest);

        if (response.getItems().isEmpty()) {
            response = executeDobRangeQuery(originalCriteria, pageRequest);
        }

        return response;
    }

    private Page<PrisonerDetail> executeDobRangeQuery(PrisonerDetailSearchCriteria originalCriteria, PageRequest pageRequest) {
        PrisonerDetailSearchCriteria criteria =   PrisonerDetailSearchCriteria.builder()
                .dobFrom(originalCriteria.getDobFrom())
                .dobTo(originalCriteria.getDobTo())
                .maxYearsRange(originalCriteria.getMaxYearsRange())
                .build();

        return executeQuery(criteria, pageRequest);
    }
}
