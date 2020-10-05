package uk.gov.justice.hmpps.prison.api.resource.impl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary;
import uk.gov.justice.hmpps.prison.api.model.UpdateCaseNote;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSearchResponse;
import uk.gov.justice.hmpps.prison.api.resource.OffenderResource;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.AdjudicationSearchCriteria;
import uk.gov.justice.hmpps.prison.service.AdjudicationService;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.GlobalSearchService;
import uk.gov.justice.hmpps.prison.service.IncidentService;
import uk.gov.justice.hmpps.prison.service.InmateAlertService;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.OffenderAddressService;
import uk.gov.justice.hmpps.prison.service.PrisonerCreationService;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

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
    private final PrisonerCreationService prisonerCreationService;

    @Override
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public InmateDetail getOffender(final String offenderNo) {
        return inmateService.findOffender(offenderNo, true);
    }

    @Override
    @HasWriteScope
    @PreAuthorize("hasRole('BOOKING_CREATE')")
    @ProxyUser
    public PrisonerIdentifier getNextPrisonerIdentifier() {
        return prisonerCreationService.getNextPrisonerIdentifier();
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
                                                 final String finding,
                                                 final LocalDate fromDate, LocalDate toDate,
                                                 final Long pageOffset,
                                                 final Long pageLimit) {

        val criteria = AdjudicationSearchCriteria.builder()
                .offenderNumber(offenderNo)
                .offenceId(offenceId)
                .agencyId(agencyId)
                .findingCode(finding)
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

    @Override
    public PrivilegeSummary getLatestBookingIEPSummaryForOffender(final String offenderNo, final boolean withDetails) {
        var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        Optional.ofNullable(booking).orElseThrow(EntityNotFoundException.withId(offenderNo));
        return bookingService.getBookingIEPSummary(booking.getBookingId(), withDetails);
    }
}
