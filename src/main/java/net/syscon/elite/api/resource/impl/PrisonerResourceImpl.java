package net.syscon.elite.api.resource.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.resource.PrisonerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;

@RestResource
@Path("prisoners")
@Slf4j
public class PrisonerResourceImpl implements PrisonerResource {
    private final GlobalSearchService globalSearchService;

    public PrisonerResourceImpl(GlobalSearchService globalSearchService) {
        this.globalSearchService = globalSearchService;
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public GetPrisonersResponse getPrisoners(
            boolean includeAliases,
            String offenderNo,
            String pncNumber,
            String croNumber,
            String firstName,
            String middleNames,
            String lastName,
            String dob,
            String dobFrom,
            String dobTo,
            String location,
            String genderCode,
            boolean partialNameMatch,
            boolean prioritisedMatch,
            boolean anyMatch,
            Long pageOffset,
            Long pageLimit,
            String sortFields,
            Order sortOrder) {

        PrisonerDetailSearchCriteria criteria = PrisonerDetailSearchCriteria.builder()
                .includeAliases(includeAliases)
                .offenderNo(offenderNo)
                .firstName(firstName)
                .middleNames(middleNames)
                .lastName(lastName)
                .pncNumber(pncNumber)
                .croNumber(croNumber)
                .latestLocationId(location)
                .sexCode(genderCode)
                .dob(fromISO8601DateString(dob))
                .dobFrom(fromISO8601DateString(dobFrom))
                .dobTo(fromISO8601DateString(dobTo))
                .partialNameMatch(partialNameMatch)
                .anyMatch(anyMatch)
                .prioritisedMatch(prioritisedMatch)
                .build();

        log.info("Global Search with search criteria: {}", criteria);
        Page<PrisonerDetail> offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(sortFields, sortOrder, pageOffset, pageLimit));
        log.info("Global Search returned {} records", offenders.getTotalRecords());
        return GetPrisonersResponse.respond200WithApplicationJson(offenders);
    }

    @Override
    public GetPrisonersOffenderNoResponse getPrisonersOffenderNo(String offenderNo) {

        PrisonerDetailSearchCriteria criteria = PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .build();

        log.info("Global Search with search criteria: {}", criteria);
        Page<PrisonerDetail> offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(null, null, 0L, 1000L));
        log.debug("Global Search returned {} records", offenders.getTotalRecords());
        return GetPrisonersOffenderNoResponse.respond200WithApplicationJson(offenders.getItems());
    }
}
