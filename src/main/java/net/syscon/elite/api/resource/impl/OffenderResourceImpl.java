package net.syscon.elite.api.resource.impl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import net.syscon.elite.api.model.AddressDto;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.model.InmateDetail;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.model.OffenderNumber;
import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.model.UpdateCaseNote;
import net.syscon.elite.api.model.adjudications.AdjudicationDetail;
import net.syscon.elite.api.model.adjudications.AdjudicationSearchResponse;
import net.syscon.elite.api.resource.OffenderResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.HasWriteScope;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyOffenderAccess;
import net.syscon.elite.service.AdjudicationSearchCriteria;
import net.syscon.elite.service.AdjudicationService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.IncidentService;
import net.syscon.elite.service.InmateAlertService;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.OffenderAddressService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestController
@RequestMapping("${api.base.path}/offenders")
@RequiredArgsConstructor
public class OffenderResourceImpl implements OffenderResource {

    private final IncidentService incidentService;
    private final InmateService inmateService;
    private final InmateAlertService alertService;
    private final OffenderAddressService addressService;
    private final AdjudicationService adjudicationService;
    private final CaseNoteService caseNoteService;
    private final BookingService bookingService;
    private final GlobalSearchService globalSearchService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public InmateDetail getOffender(final String offenderNo) {
        final var booking = bookingService.getBookingIdByOffenderNo(offenderNo);
        return inmateService.findOffender(booking.getOffenderNo(), true);
    }

    @Override
    public  List<IncidentCase> getIncidentsByOffenderNo(@NotNull final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        return incidentService.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles);
    }

    @Override
    public ResponseEntity<List<String>> getIncidentCandidates(@NotNull final LocalDateTime fromDateTime, final Long pageOffset, final Long pageLimit) {
        var paged = incidentService.getIncidentCandidates(fromDateTime,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 1000L));

        return ResponseEntity.ok().headers(paged.getPaginationHeaders()).body(paged.getItems());
    }

    @Override
    public List<AddressDto> getAddressesByOffenderNo(@NotNull String offenderNo) {
        return addressService.getAddressesByOffenderNo(offenderNo);
    }

    @Override
    public ResponseEntity<AdjudicationSearchResponse> getAdjudicationsByOffenderNo(@NotNull final String offenderNo,
                                                 final String offenceId,
                                                 final String agencyId,
                                                 final LocalDate fromDate, LocalDate toDate,
                                                 final Long pageOffset,
                                                 final Long pageLimit) {

        val criteria = AdjudicationSearchCriteria.builder()
                .offenderNumber(offenderNo)
                .offenceId(offenceId)
                .agencyId(agencyId)
                .startDate(fromDate)
                .endDate(toDate)
                .pageRequest(new PageRequest(pageOffset, pageLimit))
                .build();

        val page = adjudicationService.findAdjudications(criteria);

        return ResponseEntity.ok()
                .headers(page.getPaginationHeaders())
                .body(AdjudicationSearchResponse.builder()
                        .results(page.getItems())
                        .offences(adjudicationService.findAdjudicationsOffences(criteria.getOffenderNumber()))
                        .agencies(adjudicationService.findAdjudicationAgencies(criteria.getOffenderNumber()))
                        .build());
    }

    @Override
    public AdjudicationDetail getAdjudication(@NotNull final String offenderNo, @NotNull final long adjudicationNo) {
        return adjudicationService.findAdjudication(offenderNo, adjudicationNo);
    }

    @Override
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "SYSTEM_READ_ONLY", "GLOBAL_SEARCH", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION"})
    public List<Alert> getAlertsByOffenderNo(@NotNull final String offenderNo, final Boolean latestOnly, final String query, final String sortFields, final Order sortOrder) {
        return alertService.getInmateAlertsByOffenderNos(
                offenderNo,
                nvl(latestOnly, true),
                query,
                StringUtils.defaultIfBlank(sortFields, "bookingId,alertId"),
                nvl(sortOrder, Order.ASC));
    }

    @Override
    public ResponseEntity<List<String>> getAlertCandidates(@NotNull final LocalDateTime fromDateTime, final Long pageOffset, final Long pageLimit) {
        return alertService.getAlertCandidates(fromDateTime,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 1000L)).getResponse();
    }

    @Override
    @VerifyOffenderAccess
    public ResponseEntity<List<CaseNote>> getOffenderCaseNotes(final String offenderNo, final LocalDate from, final LocalDate to, final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);

        try {
            final var pagedCaseNotes = caseNoteService.getCaseNotes(
                    latestBookingByOffenderNo.getBookingId(),
                    query,
                    from,
                    to,
                    sortFields,
                    sortOrder,
                    nvl(pageOffset, 0L),
                    nvl(pageLimit, 10L));

            return ResponseEntity.ok()
                    .headers(pagedCaseNotes.getPaginationHeaders())
                    .body(pagedCaseNotes.getItems());

        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @Override
    @VerifyOffenderAccess
    public CaseNote getOffenderCaseNote(final String offenderNo, final Long caseNoteId) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        try {
            return caseNoteService.getCaseNote(latestBookingByOffenderNo.getBookingId(), caseNoteId);
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public CaseNote createOffenderCaseNote(final String offenderNo, final NewCaseNote body) {
        try {
            return caseNoteService.createCaseNote(offenderNo, body, authenticationFacade.getCurrentUsername());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public CaseNote updateOffenderCaseNote(final String offenderNo, final Long caseNoteId, final UpdateCaseNote body) {
        try {
            return caseNoteService.updateCaseNote(offenderNo, caseNoteId, authenticationFacade.getCurrentUsername(), body.getText());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @Override
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public OffenderSentenceDetail getOffenderSentenceDetail(final String offenderNo) {
        return bookingService.getOffenderSentenceDetail(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
    }

    @Override
    public ResponseEntity<List<OffenderNumber>> getOffenderNumbers(final Long pageOffset, final Long pageLimit) {

        final var offenderNumbers = globalSearchService.getOffenderNumbers(
                nvl(pageOffset, 0L),
                nvl(pageLimit, 100L));

        return ResponseEntity.ok()
                .headers(offenderNumbers.getPaginationHeaders())
                .body(offenderNumbers.getItems());
    }
}
