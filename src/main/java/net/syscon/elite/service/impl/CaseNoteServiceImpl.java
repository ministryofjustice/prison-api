package net.syscon.elite.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.validation.CaseNoteTypeSubTypeValid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
public class CaseNoteServiceImpl implements CaseNoteService {
    private static final String AMEND_CASE_NOTE_FORMAT = "%s ...[%s updated the case notes on %s] %s";
    private static final int MAXIMUM_CHARACTER_LIMIT = 4000;

    @Value("${api.caseNote.sourceCode:AUTO}")
    private String caseNoteSource;

    private final CaseNoteRepository caseNoteRepository;
    private final CaseNoteTransformer transformer;
    private final UserService userService;
    private final AuthenticationFacade authenticationFacade;
    private final int maxBatchSize;

    public CaseNoteServiceImpl(final CaseNoteRepository caseNoteRepository, final CaseNoteTransformer transformer,
                               final UserService userService,
                               final AuthenticationFacade authenticationFacade,
                               @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.caseNoteRepository = caseNoteRepository;
        this.transformer = transformer;
        this.userService = userService;
        this.authenticationFacade = authenticationFacade;
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    @VerifyBookingAccess
    public Page<CaseNote> getCaseNotes(final Long bookingId, final String query, final LocalDate from, final LocalDate to, final String orderBy, final Order order, final long offset, final long limit) {
        final var orderByBlank = StringUtils.isBlank(orderBy);

        final var caseNotePage = caseNoteRepository.getCaseNotes(
                bookingId,
                query,
                from,
                to,
                orderByBlank ? "creationDateTime" : orderBy,
                orderByBlank ? Order.DESC : order,
                offset,
                limit);

        final var transformedCaseNotes =
                caseNotePage.getItems().stream().map(transformer::transform).collect(Collectors.toList());

        log.info("Returning {} out of {} matching Case Notes, starting at {} for booking id {}", transformedCaseNotes.size(), caseNotePage.getTotalRecords(), caseNotePage.getPageOffset(), bookingId);

        return new Page<>(transformedCaseNotes, caseNotePage.getTotalRecords(), caseNotePage.getPageOffset(), caseNotePage.getPageLimit());
    }

    @Override
    @VerifyBookingAccess
    public CaseNote getCaseNote(final Long bookingId, final Long caseNoteId) {
        final var caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId)
                .orElseThrow(EntityNotFoundException.withId(caseNoteId));

        log.info("Returning case note {}", caseNote);

        return transformer.transform(caseNote);
    }

    @Override
    @Transactional
    @VerifyBookingAccess
    public CaseNote createCaseNote(final Long bookingId, @NotNull @Valid @CaseNoteTypeSubTypeValid final NewCaseNote caseNote, final String username) {
        final var userDetail = userService.getUserByUsername(username);
        // TODO: For Elite - check Booking Id Sealed status. If status is not sealed then allow to add Case Note.
        final var caseNoteId = caseNoteRepository.createCaseNote(bookingId, caseNote, caseNoteSource, userDetail.getUsername(), userDetail.getStaffId());

        return getCaseNote(bookingId, caseNoteId);
    }

    @Override
    @Transactional
    @VerifyBookingAccess
    public CaseNote updateCaseNote(final Long bookingId, final Long caseNoteId, final String username, @NotBlank(message = "{caseNoteTextBlank}") final String newCaseNoteText) {
        final var caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId)
                .orElseThrow(EntityNotFoundException.withId(caseNoteId));

        // Verify that user attempting to amend case note is same one who created it.
        final var userDetail = userService.getUserByUsername(username);
        final var bypassCaseNoteAmendmentRestriction = authenticationFacade.isOverrideRole("CASE_NOTE_ADMIN");

        if (!bypassCaseNoteAmendmentRestriction && !caseNote.getStaffId().equals(userDetail.getStaffId())) {
            throw new AccessDeniedException("User not authorised to amend case note.");
        }

        final var amendedText = format(AMEND_CASE_NOTE_FORMAT,
                caseNote.getText(),
                username,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
                newCaseNoteText);

        if (amendedText.length() > MAXIMUM_CHARACTER_LIMIT) {

            final var spaceLeft = MAXIMUM_CHARACTER_LIMIT - (caseNote.getText().length() + (amendedText.length() - newCaseNoteText.length()));

            final var errorMessage = spaceLeft <= 0 ?
                    "Amendments can no longer be made due to the maximum character limit being reached" :
                    format("Length should not exceed %d characters", spaceLeft);

            throw new BadRequestException(errorMessage);
        }

        caseNoteRepository.updateCaseNote(bookingId, caseNoteId, amendedText, username);
        return getCaseNote(bookingId, caseNoteId);
    }

    @Override
    @VerifyBookingAccess
    public CaseNoteCount getCaseNoteCount(final Long bookingId, final String type, final String subType, final LocalDate fromDate, final LocalDate toDate) {
        // Validate date range
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
            throw new BadRequestException("Invalid date range: toDate is before fromDate.");
        }

        final var count = caseNoteRepository.getCaseNoteCount(bookingId, type, subType, fromDate, toDate);

        return CaseNoteCount.builder()
                .bookingId(bookingId)
                .type(type)
                .subType(subType)
                .fromDate(fromDate)
                .toDate(toDate)
                .count(count)
                .build();
    }

    @Override
    public List<ReferenceCode> getCaseNoteTypesByCaseLoadType(final String caseLoadType) {
        return caseNoteRepository.getCaseNoteTypesByCaseLoadType(caseLoadType);
    }

    @Override
    public List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(final String caseLoadType) {
        return caseNoteRepository.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);
    }

    @Override
    public List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes() {
        return caseNoteRepository.getUsedCaseNoteTypesWithSubTypes();
    }

    @Override
    public List<CaseNoteUsage> getCaseNoteUsage(final String type, final String subType, final List<String> offenderNos, final Integer staffId, final String agencyId, final LocalDate fromDate, final LocalDate toDate, final int numMonths) {
        final var deriveDates = new DeriveDates(fromDate, toDate, numMonths);
        final var caseNoteUsage = new ArrayList<CaseNoteUsage>();

        if (offenderNos != null && !offenderNos.isEmpty()) {
            Lists.partition(offenderNos, maxBatchSize).forEach(offenderNosList ->
                    caseNoteUsage.addAll(
                            caseNoteRepository.getCaseNoteUsage(deriveDates.getFromDateToUse(), deriveDates.getToDateToUse(), agencyId, offenderNosList, staffId, type, subType)
                    )
            );
        } else {
            caseNoteUsage.addAll(caseNoteRepository.getCaseNoteUsage(deriveDates.getFromDateToUse(), deriveDates.getToDateToUse(), agencyId, null, staffId, type, subType));
        }
        return caseNoteUsage;
    }

    @Override
    public List<CaseNoteUsageByBookingId> getCaseNoteUsageByBookingId(final String type, final String subType, @NotEmpty final List<Integer> bookingIds, final LocalDate fromDate, final LocalDate toDate, final int numMonths) {
        final var deriveDates = new DeriveDates(fromDate, toDate, numMonths);

        return caseNoteRepository.getCaseNoteUsageByBookingId(type, subType, bookingIds, deriveDates.getFromDateToUse(), deriveDates.getToDateToUse());
    }

    @Override
    public List<CaseNoteStaffUsage> getCaseNoteStaffUsage(final String type, final String subType, @NotEmpty final List<Integer> staffIds, final LocalDate fromDate, final LocalDate toDate, final int numMonths) {
        final var deriveDates = new DeriveDates(fromDate, toDate, numMonths);

        final List<CaseNoteStaffUsage> caseNoteStaffUsage = new ArrayList<>();
        Lists.partition(staffIds, maxBatchSize).forEach(staffIdList ->
                caseNoteStaffUsage.addAll(
                        caseNoteRepository.getCaseNoteStaffUsage(type, subType, staffIdList, deriveDates.getFromDateToUse(), deriveDates.getToDateToUse())
                )
        );
        return caseNoteStaffUsage;
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER','CASE_NOTE_EVENTS')")
    public List<CaseNoteEvent> getCaseNotesEvents(final List<String> noteTypes, final LocalDateTime createdDate) {
        return getCaseNotesEvents(noteTypes, createdDate, Long.MAX_VALUE);
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER','CASE_NOTE_EVENTS')")
    public List<CaseNoteEvent> getCaseNotesEvents(final List<String> noteTypes, final LocalDateTime createdDate, final Long limit) {
        final var noteTypesMap = QueryParamHelper.splitTypes(noteTypes);

        final var events = caseNoteRepository.getCaseNoteEvents(createdDate, noteTypesMap.keySet(), limit);

        // now filter out notes based on required note types
        return events.stream().filter((event) -> {
            final var subTypes = noteTypesMap.get(event.getMainNoteType());
            // will be null if not in map, otherwise will be empty if type in map with no sub type set
            return subTypes != null && (subTypes.isEmpty() || subTypes.contains(event.getSubNoteType()));
        }).collect(Collectors.toList());
    }

    private static class DeriveDates {
        private LocalDate fromDateToUse;
        private LocalDate toDateToUse;

        DeriveDates(final LocalDate fromDate, final LocalDate toDate, final int numMonths) {
            final var now = LocalDate.now();
            fromDateToUse = now.minusMonths(numMonths);
            toDateToUse = now;

            if (fromDate != null && toDate != null) {
                fromDateToUse = fromDate;
                toDateToUse = toDate;
            } else if (fromDate != null) {
                fromDateToUse = fromDate;
                toDateToUse = fromDate.plusMonths(numMonths);
            } else if (toDate != null) {
                fromDateToUse = toDate.minusMonths(numMonths);
                toDateToUse = toDate;
            }

            toDateToUse = toDateToUse.plusDays(1);
        }

        LocalDate getFromDateToUse() {
            return fromDateToUse;
        }

        LocalDate getToDateToUse() {
            return toDateToUse;
        }
    }
}
