package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.OffenderCurfew;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.BadRequestException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

@Service
@Transactional
@Validated
public class OffenderCurfewServiceImpl implements OffenderCurfewService {

    private static final int DAYS_TO_ADD = 28;

    /**
     * Comparator for sorting OffenderCurfew instances by HDCED (HomeDetentionCurfewEligibilityDate). Nulls sort high.
     */
    private static final Comparator<OffenderSentenceCalculation> HDCED_COMPARATOR =
            comparing(
                    OffenderSentenceCalculation::getHomeDetCurfEligibilityDate,
                    nullsLast(naturalOrder())
            );

    /**
     * A Comparator to be used to find the current OffenderCurfew for a particular OffenderBookId.
     * Compares by assessmentDate (nulls sort high), and then by offenderCurfewId.  This is meant to be used to select
     * a single OffenderCurfew instance from a set whose members all have the same offenderBookId.
     */
    static final Comparator<OffenderCurfew> OFFENDER_CURFEW_COMPARATOR =
            comparing(OffenderCurfew::getAssessmentDate, nullsLast(naturalOrder()))
                    .thenComparing(OffenderCurfew::getOffenderCurfewId);

    private final OffenderCurfewRepository offenderCurfewRepository;
    private final CaseloadToAgencyMappingService caseloadToAgencyMappingService;
    private final BookingService bookingService;
    private final ReferenceDomainService referenceDomainService;
    private final Clock clock;

    public OffenderCurfewServiceImpl(
            final OffenderCurfewRepository offenderCurfewRepository,
            final CaseloadToAgencyMappingService caseloadToAgencyMappingService,
            final BookingService bookingService,
            final ReferenceDomainService referenceDomainService,
            final Clock clock) {
        this.offenderCurfewRepository = offenderCurfewRepository;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
        this.clock = clock;
    }

    @Override
    public List<OffenderSentenceCalc> getHomeDetentionCurfewCandidates(final String username, Optional<LocalDate> minimumChecksPassedDateForAssessedCurfews) {

        final var earliestArdOrCrd = LocalDate.now(clock).plusDays(DAYS_TO_ADD);
        final var agencyIds = agencyIdsFor(username);

        final var homeDetentionCurfewCandidates = getHomeDetentionCurfewCandidates(
                        offenderCurfewRepository.offenderCurfews(agencyIds),
                        earliestArdOrCrd,
                        bookingService.getOffenderSentenceCalculationsForAgency(agencyIds),
                minimumChecksPassedDateForAssessedCurfews);

        return homeDetentionCurfewCandidates.stream()
                .map(os -> OffenderSentenceCalc.builder()
                        .bookingId(os.getBookingId())
                        .offenderNo(os.getOffenderNo())
                        .firstName(os.getFirstName())
                        .lastName(os.getLastName())
                        .agencyLocationId(os.getAgencyLocationId())
                        .sentenceDetail(BaseSentenceDetail.builder()
                                .sentenceExpiryDate(os.getSentenceExpiryDate())
                                .homeDetentionCurfewEligibilityDate(os.getHomeDetCurfEligibilityDate())
                                .homeDetentionCurfewActualDate(os.getHomeDetCurfActualDate())
                                .automaticReleaseDate(os.getAutomaticReleaseDate())
                                .conditionalReleaseDate(os.getConditionalReleaseDate())
                                .nonParoleDate(os.getNonParoleDate())
                                .postRecallReleaseDate(os.getPostRecallReleaseDate())
                                .licenceExpiryDate(os.getLicenceExpiryDate())
                                .paroleEligibilityDate(os.getParoleEligibilityDate())
                                .actualParoleDate(os.getActualParolDate())
                                .releaseOnTemporaryLicenceDate(os.getRotl())
                                .earlyRemovalSchemeEligibilityDate(os.getErsed())
                                .earlyTermDate(os.getEarlyTermDate())
                                .midTermDate(os.getMidTermDate())
                                .lateTermDate(os.getLateTermDate())
                                .topupSupervisionExpiryDate(os.getTopupSupervisionExpiryDate())
                                .tariffDate(os.getTariffDate())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM_USER')")
    public HomeDetentionCurfew getLatestHomeDetentionCurfew(final long bookingId) {
        return offenderCurfewRepository
                .getLatestHomeDetentionCurfew(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("No 'latest' Home Detention Curfew found for bookingId " + bookingId));
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('SYSTEM_USER')")
    public void setHdcChecks(final long bookingId, final HdcChecks hdcChecks) {
        offenderCurfewRepository.setHDCChecksPassed(bookingId, hdcChecks);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('SYSTEM_USER')")
    public void setApprovalStatus(final long bookingId, final ApprovalStatus approvalStatus) {
        if (!referenceDomainService.isReferenceCodeActive("HDC_APPROVE", approvalStatus.getApprovalStatus())) {
            throw new BadRequestException(String.format("Approval status code '%1$s' not found and active.", approvalStatus));
        }
        final var refusedReason = approvalStatus.getRefusedReason();
        if (refusedReason != null) {
            if (!referenceDomainService.isReferenceCodeActive("HDC_REJ_RSN", refusedReason)) {
                throw new BadRequestException(String.format("Refused reason code '%1$s' not found and active.", approvalStatus));
            }
        }
        offenderCurfewRepository.setApprovalStatusForLatestCurfew(bookingId, approvalStatus);
    }

    private Set<String> agencyIdsFor(final String username) {
        return caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(username)
                .stream()
                .map(Agency::getAgencyId)
                .collect(toSet());
    }

    static List<OffenderSentenceCalculation> getHomeDetentionCurfewCandidates(
            final Collection<OffenderCurfew> curfews,
            final LocalDate earliestArdOrCrd,
            final List<OffenderSentenceCalculation> offenderSentences,
            final Optional<LocalDate> minimumChecksPassedDateForAssessedCurfews) {

        final var offenderBookingIdsForNewHDCProcess = offenderBookingIdsForNewHDCProcess(currentOffenderCurfews(curfews), minimumChecksPassedDateForAssessedCurfews);

        return offenderSentences
                .stream()
                .filter(offenderIsEligibleForHomeCurfew(offenderBookingIdsForNewHDCProcess, earliestArdOrCrd))
                .sorted(HDCED_COMPARATOR)
                .collect(toList());
    }


    /**
     * Given a Collection of OffenderCurfew where there may be more than one per offenderBookId, select, for each
     * offenderBookId the 'current' OffenderCurfew.  This is the instance that sorts highest by OFFENDER_CURFEW_COMPARATOR.
     *
     * @param curfews The curfews to sift
     * @return The current curfew for each offenderBookId
     */
    static Stream<OffenderCurfew> currentOffenderCurfews(final Collection<OffenderCurfew> curfews) {

        final var currentByOffenderBookdId = curfews
                .stream()
                .collect(
                    groupingBy(
                        OffenderCurfew::getOffenderBookId,
                        maxBy(OFFENDER_CURFEW_COMPARATOR)
                    ));

        return currentByOffenderBookdId
                .values()
                .stream()
                .map(opt -> opt.orElseThrow(() -> new NullPointerException("Impossible")));
    }

    static Set<Long> offenderBookingIdsForNewHDCProcess(
            final Stream<OffenderCurfew> currentOffenderCurfews,
            final Optional<LocalDate> minimumChecksPassedDateForAssessedCurfews) {

        return currentOffenderCurfews.filter(oc ->
                        oc.getApprovalStatus() == null ||
                        minimumDateFilter(oc.getAssessmentDate(), minimumChecksPassedDateForAssessedCurfews))
                .map(OffenderCurfew::getOffenderBookId)
                .collect(toSet());
    }


    private static boolean minimumDateFilter(LocalDate assessmentDate, Optional<LocalDate> minimumDate) {
        return Optional
                .ofNullable(assessmentDate)
                .map( ad ->
                        minimumDate
                        .map(md -> ad.isEqual(md) || ad.isAfter(md))
                        .orElse(Boolean.TRUE))
                .orElse(Boolean.FALSE);
    }

    static Predicate<OffenderSentenceCalculation> offenderIsEligibleForHomeCurfew(
            final Set<Long> offendersWithoutCurfewApprovalStatus,
            final LocalDate earliestArdOrCrd) {

        return (OffenderSentenceCalculation os) -> (os.getHomeDetCurfEligibilityDate() != null) &&
        offendersWithoutCurfewApprovalStatus.contains(os.getBookingId()) &&
        (
            isBeforeOrEqual(earliestArdOrCrd, os.getAutomaticReleaseDate()) || isBeforeOrEqual(earliestArdOrCrd, os.getConditionalReleaseDate())
        );
    }

    private static boolean isBeforeOrEqual(final LocalDate d1, final LocalDate d2) {
        return
            d2 != null &&
            (
                d1.isBefore(d2) ||
                d1.isEqual(d2)
            );
    }
}
