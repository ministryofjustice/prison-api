package net.syscon.elite.api.resource.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.model.PrisonerDetailSearchCriteria;
import net.syscon.elite.api.resource.PrisonerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.GlobalSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;

@RestController
@RequestMapping("prisoners")
@Slf4j
@AllArgsConstructor
public class PrisonerResourceImpl implements PrisonerResource {
    private final GlobalSearchService globalSearchService;

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public ResponseEntity<List<PrisonerDetail>> getPrisoners(
            final boolean includeAliases,
            final List<String> offenderNos,
            final String pncNumber,
            final String croNumber,
            final String firstName,
            final String middleNames,
            final String lastName,
            final LocalDate dob,
            final LocalDate dobFrom,
            final LocalDate dobTo,
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
                .dob(dob)
                .dobFrom(dobFrom)
                .dobTo(dobTo)
                .partialNameMatch(partialNameMatch)
                .anyMatch(anyMatch)
                .prioritisedMatch(prioritisedMatch)
                .build();

        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(sortFields, sortOrder, pageOffset, pageLimit));
        log.info("Global Search returned {} records", offenders.getTotalRecords());
        return ResponseEntity.ok()
                .headers(offenders.getPaginationHeaders())
                .body(offenders.getItems());
    }

    @Override
    public List<PrisonerDetail> getPrisonersOffenderNo(final String offenderNo) {

        final var criteria = PrisonerDetailSearchCriteria.builder()
                .offenderNos(List.of(offenderNo))
                .build();

        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(null, null, 0L, 1000L));
        log.debug("Global Search returned {} records", offenders.getTotalRecords());
        return offenders.getItems();
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'GLOBAL_SEARCH')")
    public ResponseEntity<List<PrisonerDetail>> getPrisoners(final PrisonerDetailSearchCriteria criteria,
                                                             final Long pageOffset,
                                                             final Long pageLimit,
                                                             final String sortFields,
                                                             final Order sortOrder) {
        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(sortFields, sortOrder, pageOffset, pageLimit));
        log.debug("Global Search returned {} records", offenders.getTotalRecords());
        return ResponseEntity.ok()
                .headers(offenders.getPaginationHeaders())
                .body(offenders.getItems());
    }

}
