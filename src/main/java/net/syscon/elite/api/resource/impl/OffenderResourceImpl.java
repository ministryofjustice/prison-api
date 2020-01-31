package net.syscon.elite.api.resource.impl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.model.adjudications.AdjudicationDetail;
import net.syscon.elite.api.model.adjudications.AdjudicationSearchResponse;
import net.syscon.elite.api.resource.BookingResource.GetAlertsByOffenderNosResponse;
import net.syscon.elite.api.resource.IncidentsResource.IncidentListResponse;
import net.syscon.elite.api.resource.OffenderResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyOffenderAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.impl.IncidentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;
import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/offenders")
@RequiredArgsConstructor
public class OffenderResourceImpl implements OffenderResource {

    private final IncidentService incidentService;
    private final InmateAlertService alertService;
    private final OffenderAddressService addressService;
    private final AdjudicationService adjudicationService;
    private final CaseNoteService caseNoteService;
    private final BookingService bookingService;
    private final OffenderDataComplianceService offenderDataComplianceService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public IncidentListResponse getIncidentsByOffenderNo(@NotNull final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        return new IncidentListResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(),
                incidentService.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles));
    }

    @Override
    public Response getIncidentCandidates(@NotNull final LocalDateTime fromDateTime, final Long pageOffset, final Long pageLimit) {
        var paged = incidentService.getIncidentCandidates(fromDateTime,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 1000L));
        return Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("Total-Records", paged.getTotalRecords())
                .header("Page-Offset", paged.getPageOffset())
                .header("Page-Limit", paged.getPageLimit())
                .entity(paged.getItems()).build();
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
    public Response getAlertCandidates(@NotNull final LocalDateTime fromDateTime, final Long pageOffset, final Long pageLimit) {
        var paged = alertService.getAlertCandidates(fromDateTime,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 1000L));
        return Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("Total-Records", paged.getTotalRecords())
                .header("Page-Offset", paged.getPageOffset())
                .header("Page-Limit", paged.getPageLimit())
                .entity(paged.getItems()).build();
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
    @PreAuthorize("#oauth2.hasScope('write')")
    @ProxyUser
    public CaseNote createOffenderCaseNote(final String offenderNo, final NewCaseNote body) {
        try {
            return caseNoteService.createCaseNote(offenderNo, body, authenticationFacade.getCurrentUsername());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
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
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('DELETE_OFFENDER')")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteOffender(final String offenderNo) {
        offenderDataComplianceService.deleteOffender(offenderNo);
    }

    @Override
    public Response getOffenderNumbers(final Long pageOffset, final Long pageLimit) {

        final var offenderNumbers = offenderDataComplianceService.getOffenderNumbers(
                nvl(pageOffset, 0L),
                nvl(pageLimit, 100L));

        return Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("Total-Records", offenderNumbers.getTotalRecords())
                .header("Page-Offset", offenderNumbers.getPageOffset())
                .header("Page-Limit", offenderNumbers.getPageLimit())
                .entity(offenderNumbers.getItems()).build();
    }
}
