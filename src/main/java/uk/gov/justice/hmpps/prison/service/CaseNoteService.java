package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteCount;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CaseNoteFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.CaseNoteTransformer;
import uk.gov.justice.hmpps.prison.service.validation.CaseNoteTypeSubTypeValid;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSizeValidator;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
public class CaseNoteService {

    @Value("${api.caseNote.sourceCode:AUTO}")
    private String caseNoteSource;

    private final CaseNoteRepository caseNoteRepository;
    private final OffenderCaseNoteRepository offenderCaseNoteRepository;
    private final CaseNoteTransformer transformer;
    private final BookingService bookingService;
    private final AuthenticationFacade authenticationFacade;
    private final int maxBatchSize;
    private final MaximumTextSizeValidator maximumTextSizeValidator;
    private final OffenderBookingRepository offenderBookingRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository;
    private final ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository;

    public CaseNoteService(final CaseNoteRepository caseNoteRepository,
                           final OffenderCaseNoteRepository offenderCaseNoteRepository,
                           final CaseNoteTransformer transformer,
                           final AuthenticationFacade authenticationFacade,
                           final BookingService bookingService,
                           @Value("${batch.max.size:1000}") final int maxBatchSize,
                           final MaximumTextSizeValidator maximumTextSizeValidator,
                           final OffenderBookingRepository offenderBookingRepository,
                           final StaffUserAccountRepository staffUserAccountRepository,
                           final ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository,
                           final ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository
                           ) {
        this.caseNoteRepository = caseNoteRepository;
        this.offenderCaseNoteRepository = offenderCaseNoteRepository;
        this.transformer = transformer;
        this.bookingService = bookingService;
        this.authenticationFacade = authenticationFacade;
        this.maxBatchSize = maxBatchSize;
        this.maximumTextSizeValidator = maximumTextSizeValidator;
        this.offenderBookingRepository = offenderBookingRepository;
        this.caseNoteTypeReferenceCodeRepository = caseNoteTypeReferenceCodeRepository;
        this.caseNoteSubTypeReferenceCodeRepository = caseNoteSubTypeReferenceCodeRepository;
        this.staffUserAccountRepository = staffUserAccountRepository;
    }

    public Page<CaseNote> getCaseNotes(final CaseNoteFilter caseNoteFilter, final Pageable pageable) {
        final var pagedListOfCaseNotes = offenderCaseNoteRepository.findAll(caseNoteFilter, pageable);
        final var transformedCaseNotes = pagedListOfCaseNotes.stream().map(transformer::transform).toList();

        log.info("Returning {} out of {} matching Case Notes, starting at {} for booking id {}", transformedCaseNotes.size(), pagedListOfCaseNotes.getTotalElements(), pagedListOfCaseNotes.getPageable().getOffset(), caseNoteFilter.getBookingId());
        return new PageImpl<>(transformedCaseNotes, pageable, pagedListOfCaseNotes.getTotalElements());
    }

    @VerifyBookingAccess
    public CaseNote getCaseNote(final Long bookingId, final Long caseNoteId) {
        final var caseNote = offenderCaseNoteRepository.findByIdAndOffenderBooking_BookingId(caseNoteId, bookingId)
                .orElseThrow(EntityNotFoundException.withId(caseNoteId));

        return transformer.transform(caseNote);
    }

    @Transactional
    @VerifyBookingAccess
    public CaseNote createCaseNote(final Long bookingId, @NotNull @Valid @CaseNoteTypeSubTypeValid final NewCaseNote caseNote, final String username) {
        final var userDetail = staffUserAccountRepository.findById(username).orElseThrow(EntityNotFoundException.withId(username));
        final var booking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        final var occurrenceTime = caseNote.getOccurrenceDateTime() == null ? LocalDateTime.now() : caseNote.getOccurrenceDateTime();

        final var newCaseNote = OffenderCaseNote.builder()
            .caseNoteText(caseNote.getText())
            .agencyLocation(booking.getLocation())
            .type(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk(caseNote.getType())).orElseThrow(EntityNotFoundException.withId(caseNote.getType())))
            .subType(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk(caseNote.getSubType())).orElseThrow(EntityNotFoundException.withId(caseNote.getSubType())))
            .noteSourceCode(caseNoteSource)
            .author(userDetail.getStaff())
            .occurrenceDateTime(occurrenceTime)
            .occurrenceDate(occurrenceTime.toLocalDate())
            .amendmentFlag(false)
            .offenderBooking(booking)
            .build();

        return transformer.transform(offenderCaseNoteRepository.save(newCaseNote));
    }

    @Transactional
    @VerifyBookingAccess
    public CaseNote updateCaseNote(final Long bookingId, final Long caseNoteId, final String username, @NotBlank(message = "{caseNoteTextBlank}") final String newCaseNoteText) {
        final var caseNote = offenderCaseNoteRepository.findByIdAndOffenderBooking_BookingId(caseNoteId, bookingId)
                .orElseThrow(EntityNotFoundException.withId(caseNoteId));

        // Verify that user attempting to amend case note is same one who created it.
        final var userDetail = staffUserAccountRepository.findById(username).orElseThrow(EntityNotFoundException.withId(username));
        final var bypassCaseNoteAmendmentRestriction = authenticationFacade.isOverrideRole("CASE_NOTE_ADMIN");

        if (!bypassCaseNoteAmendmentRestriction && !caseNote.getAuthor().equals(userDetail.getStaff())) {
            throw new AccessDeniedException("User not authorised to amend case note.");
        }

        final var appendedText = caseNote.createAppendedText(newCaseNoteText, username);

        if (!maximumTextSizeValidator.isValid(appendedText, null)) {

            final var spaceLeft = maximumTextSizeValidator.getMaximumAnsiEncodingSize() - (caseNote.getCaseNoteText().length() + (appendedText.length() - newCaseNoteText.length()));

            final var errorMessage = spaceLeft <= 0 ?
                    "Amendments can no longer be made due to the maximum character limit being reached" :
                    format("Length should not exceed %d characters", spaceLeft);

            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        caseNote.setCaseNoteText(appendedText);
        return transformer.transform(caseNote);
    }

    @Transactional
    @VerifyOffenderAccess
    public CaseNote createCaseNote(String offenderNo, @NotNull @Valid @CaseNoteTypeSubTypeValid NewCaseNote caseNote, String username) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return createCaseNote(latestBookingByOffenderNo.getBookingId(), caseNote, username);
    }

    @Transactional
    @VerifyOffenderAccess
    public CaseNote updateCaseNote(String offenderNo, Long caseNoteId, String username, @NotBlank(message = "{caseNoteTextBlank}") String newCaseNoteText) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return updateCaseNote(latestBookingByOffenderNo.getBookingId(), caseNoteId, username, newCaseNoteText);
    }

    @VerifyBookingAccess
    public CaseNoteCount getCaseNoteCount(final Long bookingId, final String type, final String subType, final LocalDate fromDate, final LocalDate toDate) {
        // Validate date range
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid date range: toDate is before fromDate.");
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


    public List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(final String caseLoadType) {
        return caseNoteRepository.getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag(caseLoadType, true);
    }

    public List<ReferenceCode> getInactiveCaseNoteTypesWithSubTypesByCaseLoadType(final String caseLoadType) {
        return caseNoteRepository.getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag(caseLoadType, false);
    }

    public List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes() {
        return caseNoteRepository.getUsedCaseNoteTypesWithSubTypes();
    }

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

    public List<CaseNoteUsageByBookingId> getCaseNoteUsageByBookingId(final String type, final String subType, @NotEmpty final List<Integer> bookingIds, final LocalDate fromDate, final LocalDate toDate, final int numMonths) {
        final var deriveDates = new DeriveDates(fromDate, toDate, numMonths);

        return caseNoteRepository.getCaseNoteUsageByBookingId(type, subType, bookingIds, deriveDates.getFromDateToUse(), deriveDates.getToDateToUse());
    }

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

    @PreAuthorize("hasAnyRole('SYSTEM_USER','CASE_NOTE_EVENTS')")
    public List<CaseNoteEvent> getCaseNotesEvents(final List<String> noteTypes, @NotNull final LocalDateTime createdDate) {
        return getCaseNotesEvents(noteTypes, createdDate, Long.MAX_VALUE);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER','CASE_NOTE_EVENTS')")
    public List<CaseNoteEvent> getCaseNotesEvents(@NotEmpty final List<String> noteTypes, @NotNull final LocalDateTime createdDate, @Min(1) @Max(5000) @NotNull final Long limit) {
        final var noteTypesMap = QueryParamHelper.splitTypes(noteTypes);

        final var events = caseNoteRepository.getCaseNoteEvents(createdDate, noteTypesMap.keySet(), limit);

        // now filter out notes based on required note types
        return events.stream().filter((event) -> {
            final var subTypes = noteTypesMap.get(event.getMainNoteType());
            // will be null if not in map, otherwise will be empty if type in map with no sub type set
            return subTypes != null && (subTypes.isEmpty() || subTypes.contains(event.getSubNoteType()));
        }).toList();
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

