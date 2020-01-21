package net.syscon.elite.api.resource.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.PrisonerDetailSearchCriteria;
import net.syscon.elite.api.resource.PrisonerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.impl.PrisonerInformationService;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;

@RestResource
@Path("prisoners")
@Slf4j
@AllArgsConstructor
public class PrisonerResourceImpl implements PrisonerResource {
    private final GlobalSearchService globalSearchService;
    private final PrisonerInformationService prisonerInformationService;

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public GetPrisonersResponse getPrisoners(
            final boolean includeAliases,
            final List<String> offenderNos,
            final String pncNumber,
            final String croNumber,
            final String firstName,
            final String middleNames,
            final String lastName,
            final String dob,
            final String dobFrom,
            final String dobTo,
            final String location,
            final String genderCode,
            final boolean partialNameMatch,
            final boolean prioritisedMatch,
            final boolean anyMatch,
            final Long pageOffset,
            final Long pageLimit,
            final String sortFields,
            final Order sortOrder) {

        final var criteria = PrisonerDetailSearchCriteria.builder()
                .includeAliases(includeAliases)
                .offenderNos(offenderNos)
                .firstName(firstName)
                .middleNames(middleNames)
                .lastName(lastName)
                .pncNumber(pncNumber)
                .croNumber(croNumber)
                .location(location)
                .gender(genderCode)
                .dob(fromISO8601DateString(dob))
                .dobFrom(fromISO8601DateString(dobFrom))
                .dobTo(fromISO8601DateString(dobTo))
                .partialNameMatch(partialNameMatch)
                .anyMatch(anyMatch)
                .prioritisedMatch(prioritisedMatch)
                .build();

        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(sortFields, sortOrder, pageOffset, pageLimit));
        log.info("Global Search returned {} records", offenders.getTotalRecords());
        return GetPrisonersResponse.respond200WithApplicationJson(offenders);
    }

    @Override
    public GetPrisonersOffenderNoResponse getPrisonersOffenderNo(final String offenderNo) {

        final var criteria = PrisonerDetailSearchCriteria.builder()
                .offenderNos(List.of(offenderNo))
                .build();

        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(null, null, 0L, 1000L));
        log.debug("Global Search returned {} records", offenders.getTotalRecords());
        return GetPrisonersOffenderNoResponse.respond200WithApplicationJson(offenders.getItems());
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public GetPrisonersResponse getPrisoners(final PrisonerDetailSearchCriteria criteria,
                                             final Long pageOffset,
                                             final Long pageLimit,
                                             final String sortFields,
                                             final Order sortOrder) {
        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(sortFields, sortOrder, pageOffset, pageLimit));
        log.debug("Global Search returned {} records", offenders.getTotalRecords());
        return GetPrisonersResponse.respond200WithApplicationJson(offenders);
    }

}
