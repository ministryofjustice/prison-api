package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.NonDtoReleaseDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Bookings API service implementation.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final AgencyService agencyService;

    public BookingServiceImpl(BookingRepository bookingRepository, AgencyService agencyService) {
        this.bookingRepository = bookingRepository;
        this.agencyService = agencyService;
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

    @Override
    public List<ScheduledEvent> getBookingActivities(Long bookingId) {
        verifyBookingAccess(bookingId);

        return new ArrayList<>();
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

    /**
     * Gets set of agency location ids accessible to current authenticated user. This governs access to bookings - a user
     * cannot have access to an offender unless they are in a location that the authenticated user is also associated with.
     *
     * @return set of agency location ids accessible to current authenticated user.
     */
    private Set<String> getAgencyIds() {
        return agencyService
                .findAgenciesByUsername(UserSecurityUtils.getCurrentUsername())
                .stream()
                .map(Agency::getAgencyId)
                .collect(Collectors.toSet());
    }

    /**
     * Verifies that current user is authorised to access specified offender booking. If offender booking is in an
     * agency location that is not part of any caseload accessible to the current user, a 'Resource Not Found'
     * exception is thrown.
     *
     * @param bookingId offender booking id.
     * @throws EntityNotFoundException if current user does not have access to specified booking.
     */
    private void verifyBookingAccess(Long bookingId) {
        if (!bookingRepository.verifyBookingAccess(bookingId, getAgencyIds())) {
            throw new EntityNotFoundException(bookingId.toString());
        }
    }
}
