package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.PrivilegeDetail;
import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.NonDtoReleaseDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Bookings API (v2) service implementation.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public SentenceDetail getBookingSentenceDetail(Long bookingId) {
        SentenceDetail sentenceDetail = bookingRepository.getBookingSentenceDetail(bookingId).orElseThrow(new EntityNotFoundException(bookingId.toString()));

        NonDtoReleaseDate nonDtoReleaseDate = deriveNonDtoReleaseDate(sentenceDetail);

        if (Objects.nonNull(nonDtoReleaseDate)) {
            sentenceDetail.setNonDtoReleaseDate(nonDtoReleaseDate.getReleaseDate());
            sentenceDetail.setNonDtoReleaseDateType(nonDtoReleaseDate.getReleaseDateType());
        }

        return sentenceDetail;
    }

    @Override
    public PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails) {
        List<PrivilegeDetail> iepDetails = bookingRepository.getBookingIEPDetails(bookingId);

        // If no IEP details exist for offender, cannot derive an IEP summary.
        if (iepDetails.isEmpty()) {
            throw new EntityNotFoundException(bookingId.toString());
        }

        // Extract most recent detail from list
        PrivilegeDetail currentDetail = iepDetails.get(0);

        // Determine number of days since current detail became effective
        long daysSinceReview = DAYS.between(currentDetail.getIepDate(), now());

        // Construct and return IEP summary.
        return PrivilegeSummary.builder()
                .bookingId(bookingId)
                .iepDate(currentDetail.getIepDate())
                .iepTime(currentDetail.getIepTime())
                .iepLevel(currentDetail.getIepLevel())
                .daysSinceReview(Long.valueOf(daysSinceReview).intValue())
                .iepDetails(withDetails ? iepDetails : Collections.EMPTY_LIST)
                .build();
    }

    private NonDtoReleaseDate deriveNonDtoReleaseDate(SentenceDetail sentenceDetail) {
        List<NonDtoReleaseDate> nonDtoReleaseDates = new ArrayList<>();

        if (Objects.nonNull(sentenceDetail.getAutomaticReleaseDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(SentenceDetail.NonDtoReleaseDateType.ARD,
                    sentenceDetail.getAutomaticReleaseDate(), false));
        }

        if (Objects.nonNull(sentenceDetail.getAutomaticReleaseOverrideDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(SentenceDetail.NonDtoReleaseDateType.ARD,
                    sentenceDetail.getAutomaticReleaseOverrideDate(), true));
        }

        if (Objects.nonNull(sentenceDetail.getConditionalReleaseDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(SentenceDetail.NonDtoReleaseDateType.CRD,
                    sentenceDetail.getConditionalReleaseDate(), false));
        }

        if (Objects.nonNull(sentenceDetail.getConditionalReleaseOverrideDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(SentenceDetail.NonDtoReleaseDateType.CRD,
                    sentenceDetail.getConditionalReleaseOverrideDate(), true));
        }

        if (Objects.nonNull(sentenceDetail.getNonParoleDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(SentenceDetail.NonDtoReleaseDateType.NPD,
                    sentenceDetail.getNonParoleDate(), false));
        }

        if (Objects.nonNull(sentenceDetail.getNonParoleOverrideDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(SentenceDetail.NonDtoReleaseDateType.NPD,
                    sentenceDetail.getNonParoleOverrideDate(), true));
        }

        if (Objects.nonNull(sentenceDetail.getPostRecallReleaseDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(SentenceDetail.NonDtoReleaseDateType.PRRD,
                    sentenceDetail.getPostRecallReleaseDate(), false));
        }

        if (Objects.nonNull(sentenceDetail.getPostRecallReleaseOverrideDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(SentenceDetail.NonDtoReleaseDateType.PRRD,
                    sentenceDetail.getPostRecallReleaseOverrideDate(), true));
        }

        Collections.sort(nonDtoReleaseDates);

        return nonDtoReleaseDates.isEmpty() ? null : nonDtoReleaseDates.get(0);
    }
}
