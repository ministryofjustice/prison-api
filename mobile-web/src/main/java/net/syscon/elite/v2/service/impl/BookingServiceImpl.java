package net.syscon.elite.v2.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.v2.api.model.PrivilegeDetail;
import net.syscon.elite.v2.api.model.PrivilegeSummary;
import net.syscon.elite.v2.api.model.SentenceDetail;
import net.syscon.elite.v2.repository.BookingRepository;
import net.syscon.elite.v2.service.BookingService;
import net.syscon.elite.v2.service.support.NonDtoReleaseDate;
import net.syscon.elite.v2.service.support.ReleaseDateType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

        // Now need to determine release date and days remaining to complete sentence detail.
        //
        // Non-DTO Sentences:
        // - release date derived from one of ARD, CRD, NPD or PRRD, in following order of priority:
        //   -  1. Latest override date if more than one ARD/CRD/NPD/PRRD override date is defined
        //   -  2. ARD override date, if defined
        //   -  3. CRD override date, if defined
        //   -  4. NPD override date, if defined
        //   -  5. PRRD override date, if defined
        //   -  6. ARD calculated date, if defined
        //   -  7. CRD calculated date, if defined
        //   -  8. NPD calculated date, if defined
        //   -  9. PRRD calculated date, if defined

        NonDtoReleaseDate nonDtoReleaseDate = deriveNonDtoReleaseDate(sentenceDetail);

        if (Objects.nonNull(nonDtoReleaseDate)) {
            sentenceDetail.setNonDtoReleaseDate(nonDtoReleaseDate.getReleaseDate());

            Long daysRemaining = calcDaysRemaining(nonDtoReleaseDate.getReleaseDate());

            sentenceDetail.setDaysRemaining(daysRemaining);
        }

        return sentenceDetail;
    }

    @Override
    public PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails) {
        List<PrivilegeDetail> iepDetails = bookingRepository.getBookingIEPDetails(bookingId);

        // TODO: Can a 'default' IEP Summary be assumed if there are no IEP details?
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
            nonDtoReleaseDates.add(new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE,
                    sentenceDetail.getAutomaticReleaseDate(), false));
        }

        if (Objects.nonNull(sentenceDetail.getAutomaticReleaseOverrideDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(ReleaseDateType.AUTOMATIC_RELEASE_DATE,
                    sentenceDetail.getAutomaticReleaseOverrideDate(), true));
        }

        if (Objects.nonNull(sentenceDetail.getConditionalReleaseDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE,
                    sentenceDetail.getConditionalReleaseDate(), false));
        }

        if (Objects.nonNull(sentenceDetail.getConditionalReleaseOverrideDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(ReleaseDateType.CONDITIONAL_RELEASE_DATE,
                    sentenceDetail.getConditionalReleaseOverrideDate(), true));
        }

        if (Objects.nonNull(sentenceDetail.getNonParoleDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE,
                    sentenceDetail.getNonParoleDate(), false));
        }

        if (Objects.nonNull(sentenceDetail.getNonParoleOverrideDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(ReleaseDateType.NON_PAROLE_DATE,
                    sentenceDetail.getNonParoleOverrideDate(), true));
        }

        if (Objects.nonNull(sentenceDetail.getPostRecallReleaseDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(ReleaseDateType.POST_RECALL_RELEASE_DATE,
                    sentenceDetail.getPostRecallReleaseDate(), false));
        }

        if (Objects.nonNull(sentenceDetail.getPostRecallReleaseOverrideDate())) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(ReleaseDateType.POST_RECALL_RELEASE_DATE,
                    sentenceDetail.getPostRecallReleaseOverrideDate(), true));
        }

        Collections.sort(nonDtoReleaseDates);

        return nonDtoReleaseDates.isEmpty() ? null : nonDtoReleaseDates.get(0);
    }

    /**
     * Calculates days between release date and current system date. If release date has already passed this method will
     * return zero, not a negative number.
     *
     * @param releaseDate release date.
     * @return number of logical days between release date and current system date (or zero if release date has already
     * passed).
     */
    private long calcDaysRemaining(LocalDate releaseDate) {
        return Math.max(0, DAYS.between(now(), releaseDate));
    }
}
