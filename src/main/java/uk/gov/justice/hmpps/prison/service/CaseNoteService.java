package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeCount;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest.BookingFromDatePair;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
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
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.CaseNoteTransformer;
import uk.gov.justice.hmpps.prison.service.validation.CaseNoteTypeSubTypeValid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
public class CaseNoteService {

    private final CaseNoteRepository caseNoteRepository;
    private final OffenderCaseNoteRepository offenderCaseNoteRepository;
    private final CaseNoteTransformer transformer;
    private final BookingService bookingService;
    private final int maxBatchSize;
    private final OffenderBookingRepository offenderBookingRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository;
    private final ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository;

    public CaseNoteService(final CaseNoteRepository caseNoteRepository,
                           final OffenderCaseNoteRepository offenderCaseNoteRepository,
                           final CaseNoteTransformer transformer,
                           final BookingService bookingService,
                           @Value("${batch.max.size:1000}") final int maxBatchSize,
                           final OffenderBookingRepository offenderBookingRepository,
                           final StaffUserAccountRepository staffUserAccountRepository,
                           final ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository,
                           final ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository
                           ) {
        this.caseNoteRepository = caseNoteRepository;
        this.offenderCaseNoteRepository = offenderCaseNoteRepository;
        this.transformer = transformer;
        this.bookingService = bookingService;
        this.maxBatchSize = maxBatchSize;
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

    public CaseNote getCaseNote(final Long bookingId, final Long caseNoteId) {
        final var caseNote = offenderCaseNoteRepository.findByIdAndOffenderBooking_BookingId(caseNoteId, bookingId)
                .orElseThrow(EntityNotFoundException.withId(caseNoteId));

        return transformer.transform(caseNote);
    }

    @Transactional
    public CaseNote createCaseNote(final Long bookingId, @NotNull @Valid @CaseNoteTypeSubTypeValid final NewCaseNote caseNote, final String username) {
        final var userDetail = staffUserAccountRepository.findById(username).orElseThrow(EntityNotFoundException.withId(username));
        final var booking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        final var occurrenceTime = caseNote.getOccurrenceDateTime() == null ? LocalDateTime.now() : caseNote.getOccurrenceDateTime();

        final var newCaseNote = OffenderCaseNote.builder()
            .caseNoteText(caseNote.getText())
            .agencyLocation(booking.getLocation())
            .type(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk(caseNote.getType())).orElseThrow(EntityNotFoundException.withId(caseNote.getType())))
            .subType(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk(caseNote.getSubType())).orElseThrow(EntityNotFoundException.withId(caseNote.getSubType())))
            .noteSourceCode(caseNote.isSystemGenerated() ? "AUTO" : "INST")
            .author(userDetail.getStaff())
            .occurrenceDateTime(occurrenceTime)
            .occurrenceDate(occurrenceTime.toLocalDate())
            .amendmentFlag(false)
            .offenderBooking(booking)
            .build();

        return transformer.transform(offenderCaseNoteRepository.save(newCaseNote));
    }

    @Transactional
    @VerifyOffenderAccess(overrideRoles = {"ADD_CASE_NOTES"})
    public CaseNote createCaseNote(String offenderNo, @NotNull @Valid @CaseNoteTypeSubTypeValid NewCaseNote caseNote, String username) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return createCaseNote(latestBookingByOffenderNo.getBookingId(), caseNote, username);
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

    public List<CaseNoteTypeCount> getCaseNoteUsageByBookingIdTypeAndDate(@NotEmpty final List<String> types, @NotEmpty final List<BookingFromDatePair> bookingReviewDatePairs) {
        final var bookingDateMap = bookingReviewDatePairs.stream().collect(Collectors.toMap(BookingFromDatePair::getBookingId, BookingFromDatePair::getFromDate));

        final var allCaseNotesOfType = offenderCaseNoteRepository.findCaseNoteTypesByBookingAndDate(
            bookingDateMap.keySet().stream().toList(),
            types,
            bookingDateMap.values().stream().min(LocalDateTime::compareTo).orElseThrow().toLocalDate()  // for performance reasons we ignore time part
        );

        return allCaseNotesOfType.stream()
            .filter(b -> !bookingDateMap.get(b.bookingId()).isAfter(b.occurrenceDateTime()))
            .collect(groupingBy(cn -> new CaseNoteTypesAndSubTypes(cn.bookingId(), cn.type(), cn.subType()), counting()))
            .entrySet().stream()
            .map(s -> new CaseNoteTypeCount(s.getKey().bookingId, s.getKey().type, s.getKey().subType, s.getValue()))
            .toList();
    }

    private record CaseNoteTypesAndSubTypes(Long bookingId, String type, String subType) {}

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
