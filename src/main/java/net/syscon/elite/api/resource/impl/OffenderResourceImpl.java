package net.syscon.elite.api.resource.impl;

import lombok.val;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.model.adjudications.AdjudicationDetail;
import net.syscon.elite.api.model.adjudications.AdjudicationSearchResponse;
import net.syscon.elite.api.resource.BookingResource.GetAlertsByOffenderNosResponse;
import net.syscon.elite.api.resource.IncidentsResource.IncidentListResponse;
import net.syscon.elite.api.resource.OffenderResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyOffenderAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.impl.IncidentService;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;
import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/offenders")
public class OffenderResourceImpl implements OffenderResource {

    private final IncidentService incidentService;
    private final InmateAlertService alertService;
    private final OffenderAddressService addressService;
    private final AdjudicationService adjudicationService;
    private final CaseNoteService caseNoteService;
    private final BookingService bookingService;
    private final AuthenticationFacade authenticationFacade;

    public OffenderResourceImpl(final IncidentService incidentService, final InmateAlertService alertService,
                                final OffenderAddressService addressService,
                                final AdjudicationService adjudicationService,
                                final CaseNoteService caseNoteService,
                                final BookingService bookingService,
                                final AuthenticationFacade authenticationFacade) {
        this.incidentService = incidentService;
        this.alertService = alertService;
        this.addressService = addressService;
        this.adjudicationService = adjudicationService;
        this.caseNoteService = caseNoteService;
        this.bookingService = bookingService;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public IncidentListResponse getIncidentsByOffenderNo(@NotNull final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        return new IncidentListResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(),
                incidentService.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles));
    }

    @Override
    public List<String> getIncidentCandidates(@NotNull final LocalDateTime fromDateTime) {
        return new ArrayList(incidentService.getIncidentCandidates(fromDateTime));
    }

    @Override
    public List<OffenderAddress> getAddressesByOffenderNo(@NotNull String offenderNo) {
        return addressService.getAddressesByOffenderNo(offenderNo);
    }

    @Override
    public Response getAdjudicationsByOffenderNo(@NotNull final String offenderNo,
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

        return Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("Total-Records", page.getTotalRecords())
                .header("Page-Offset", page.getPageOffset())
                .header("Page-Limit", page.getPageLimit())
                .entity(AdjudicationSearchResponse.builder()
                        .results(page.getItems())
                        .offences(adjudicationService.findAdjudicationsOffences(criteria.getOffenderNumber()))
                        .agencies(adjudicationService.findAdjudicationAgencies(criteria.getOffenderNumber()))
                        .build())
                .build();
    }

    @Override
    public AdjudicationDetail getAdjudication(@NotNull final String offenderNo, @NotNull final long adjudicationNo) {
        return adjudicationService.findAdjudication(offenderNo, adjudicationNo);
    }

    @Override
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "SYSTEM_READ_ONLY", "GLOBAL_SEARCH", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION"})
    public GetAlertsByOffenderNosResponse getAlertsByOffenderNo(@NotNull final String offenderNo, final Boolean latestOnly, final String query, final String sortFields, final Order sortOrder) {
        final List<Alert> inmateAlertsByOffenderNos = alertService.getInmateAlertsByOffenderNos(
                offenderNo,
                nvl(latestOnly, true),
                query,
                StringUtils.defaultIfBlank(sortFields, "bookingId,alertId"),
                nvl(sortOrder, Order.ASC));
        return GetAlertsByOffenderNosResponse.respond200WithApplicationJson(inmateAlertsByOffenderNos);
    }

    @Override
    public List<String> getAlertCandidates(@NotNull final LocalDateTime fromDateTime) {
        return alertService.getAlertCandidates(fromDateTime);
    }

    @Override
    @VerifyOffenderAccess
    public Response getOffenderCaseNotes(final String offenderNo, final String from, final String to, final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);

        try {
            final var pagedCaseNotes = caseNoteService.getCaseNotes(
                    latestBookingByOffenderNo.getBookingId(),
                    query,
                    fromISO8601DateString(from),
                    fromISO8601DateString(to),
                    sortFields,
                    sortOrder,
                    nvl(pageOffset, 0L),
                    nvl(pageLimit, 10L));

            return Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", pagedCaseNotes.getTotalRecords())
                    .header("Page-Offset", pagedCaseNotes.getPageOffset())
                    .header("Page-Limit", pagedCaseNotes.getPageLimit())
                    .entity(pagedCaseNotes.getItems()).build();
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
    @VerifyOffenderAccess
    public CaseNote createOffenderCaseNote(final String offenderNo, final NewCaseNote body) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        try {
            return caseNoteService.createCaseNote(latestBookingByOffenderNo.getBookingId(), body, authenticationFacade.getCurrentUsername());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @Override
    @VerifyOffenderAccess
    public CaseNote updateOffenderCaseNote(final String offenderNo, final Long caseNoteId, final UpdateCaseNote body) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        try {
            return caseNoteService.updateCaseNote(latestBookingByOffenderNo.getBookingId(), caseNoteId, authenticationFacade.getCurrentUsername(), body.getText());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @Override
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public OffenderSentenceDetail getOffenderSentenceDetail(final String offenderNo) {
        return bookingService.getOffenderSentenceDetail(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
    }
}
