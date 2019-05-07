package net.syscon.elite.service.impl;

import lombok.val;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.OffenderCurfew;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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

    private static final String STATUS_TRACKING_CODE_REFUSED = "REFUSED";
    private static final String STATUS_TRACKING_CODE_MANUAL_FAIL = "MAN_CK_FAIL";

    private static final Set<String> STATUS_TRACKING_CODES = Set.of(
            STATUS_TRACKING_CODE_MANUAL_FAIL,
            STATUS_TRACKING_CODE_REFUSED);

    private static final String HDC_APPROVE_DOMAIN = "HDC_APPROVE";
    private static final String HDC_REJECT_REASON_DOMAIN = "HDC_REJ_RSN";
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
        return offenderCurfewRepository.getLatestHomeDetentionCurfew(bookingId, STATUS_TRACKING_CODES)
                .orElseThrow(() -> new EntityNotFoundException("No 'latest' Home Detention Curfew found for bookingId " + bookingId));
    }

    /**
     * Set the PASSED_FLAG on the 'latest' OFFENDER_CURFEWS row for a given bookingId according to values in the HdcChecks parameter.
     * The PASSED_FLAG on an OFFENDER_CURFEWS row may only be set once. Subsequent attempts will result in an EntityNotFoundException.
     * The OFFENDER_CURFEWS and HDC_STATUS_TRACKINGS tables have triggers that act to create rows in the HDC_STATUS_TRACKINGS and HDC_STATUS_REASONS tables when
     * the OFFENDER_CURFEWS.PASSED_FLAG row is set by this method.
     * @param bookingId The OFFENDER_BOOKINGS_ID value that selects the to be updated OFFENDER_CURFEWS
     * @param hdcChecks The values to set
     * @throws EntityNotFoundException If there is no OFFENDER_CURFEWS record associated with the given bookingId.
     * @throws IllegalStateException If the PASSED_FLAG for the selected row has already been set.
     */
    @Override
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('SYSTEM_USER')")
    public void setHdcChecks(final long bookingId, final HdcChecks hdcChecks) {
        val curfew = getLatestHomeDetentionCurfew(bookingId);
        if (curfew.getPassed() != null) {
            throw new IllegalStateException("HDC Checks already set to " + curfew.getPassed());
        }
        offenderCurfewRepository.setHDCChecksPassed(curfew.getId(), hdcChecks);
    }

    /**
     * Set the APPROVAL_STATUS on the latest OFFENDER_CURFEWS record associated with the given bookingId according to the values in the ApprovalStatus parameter.
     * The APPROVAL_STATUS may only be set once for a given OFFENDER_CURFEWS record.  Subsequent attempts will result in an EntityNotFoundException.
     * @param bookingId The Booking id
     * @param approvalStatus The approval status to set
     * @throws EntityNotFoundException if there is no OFFENDER_CURFEWS record associated with the given bookingId
     * @throws IllegalStateException If the APPROVAL_STATUS for the selected row has already been set.
     */
    @Override
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('SYSTEM_USER')")
    public void setApprovalStatus(final long bookingId, final ApprovalStatus approvalStatus) {

        if (!referenceDomainService.isReferenceCodeActive(HDC_APPROVE_DOMAIN, approvalStatus.getApprovalStatus())) {
            throw new IllegalArgumentException(String.format("Approval status code '%1$s' is not a valid NOMIS value.", approvalStatus.getApprovalStatus()));
        }
        final var refusedReason = approvalStatus.getRefusedReason();
        if (refusedReason != null) {
            if (!referenceDomainService.isReferenceCodeActive(HDC_REJECT_REASON_DOMAIN, refusedReason)) {
                throw new IllegalArgumentException(String.format("Refused reason code '%1$s' is not a valid NOMIS value.", refusedReason));
            }
        }
        if (approvalStatus.isApproved() && refusedReason != null) {
            throw new IllegalArgumentException("A refused reason is not allowed when the approval status set to APPROVED.");
        }

        val curfew = getLatestHomeDetentionCurfew(bookingId);
        if (curfew.getPassed() == null) {
            throw new IllegalStateException("Checks passed has not been set.");
        }
        if (curfew.getApprovalStatus() != null) {
            throw new IllegalStateException(String.format("Approval status has already been set to %1$s.", curfew.getApprovalStatus()));
        }

        offenderCurfewRepository.setApprovalStatusForCurfew(curfew.getId(), approvalStatus);

        if (approvalStatus.isApproved()) return;

        addRefusedReason(curfew.getId(), refusedReason);
    }

    /**
     * <p>
     * What happens here depends upon whether OFFENDER_CURFEWS.PASSED_FLAG is 'Y' or 'N': NOMIS database tables
     * HDC_STATUS_TRACKINGS and HDC_STATUS_REASONS have triggers that fire when the PASSED_FLAG column is updated:
     * If PASSED_FLAG is updated to 'Y' then two records are added to both tables as follows:
     * <table>
     *     <tr><th>HDC_STATUS_TRACKINGS.STATUS_CODE</th><th>HDC_STATUS_REASONS.STATUS_REASON_CODE</th></tr>
     *     <tr><td>MAN_CK_PASS</td><td>MAN_CK</td></tr>
     *     <tr><td>ELIGIBLE</td><td>PASS_ALL_CK</td></tr>
     * </table>
     * Whereas when PASSED_FLAG is updated to 'N' two records are added to HDC_STATUS_TRACKINGS and one to HDC_STATUS_REASONS
     * <table>
     *     <tr><th>HDC_STATUS_TRACKINGS.STATUS_CODE</th><th>HDC_STATUS_REASONS.STATUS_REASON_CODE</th></tr>
     *     <tr><td>MAN_CK_FAIL</td><td></td></tr>
     *     <tr><td>INELIGIBLE</td><td>MAN_CK_FAIL</td></tr>
     * </table>
     * When NOMIS subsequently sets an Approval Status other than ACCEPTED, it also sets a Refused reason.
     * The Refused reason is stored in the STATUS_REASON_CODE column of the HDC_STATUS_REASONS table, but how it is stored
     * depends upon the current value of PASSED_FLAG. </p>
     *
     * <p> If PASSED_FLAG is 'N' then the Refused Reason is stored as a new HDC_STATUS_REASONS record that points to the
     * previously created HDC_STATUS_TRACKINGS record having STATUS_CODE MAN_CK_FAIL.</p>
     *
     * <p>If PASSED_FLAG is 'Y' then the Refused Reason is stored as a new HDC_STATUS_REASONS record that points to
     * a new HDC_STATUS_TRACKINGS record having STATUS_CODE REFUSED</p>
     *
     * <p>I believe that the current state of the HDC_STATUS_TRACKINGS table acts as a proxy for the PASSED_FLAG value
     * so long as this value is never set more than once.  If updates are only performed through the elite2api (which
     * should be the case when it is used) then this requirement can be enforced through this class. Nevertheless
     * NOMIS also enforces this constraint during normal use.</p>
     *
     * <p>Given the assumptions above the code below should be self-explanatory.</p>
     *
     * @param curfewId The curfew id for which the refused reason is to be set
     * @param refusedReason The refused reason
     */

    private void addRefusedReason(long curfewId, String refusedReason) {
        OptionalLong hdcStatusTrackingFailId = offenderCurfewRepository.findHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_MANUAL_FAIL);
        val hdcStatusTrackingId = hdcStatusTrackingFailId.orElseGet(() -> createRefusedHdcStatusTracking(curfewId));
        offenderCurfewRepository.createHdcStatusReason(hdcStatusTrackingId, refusedReason);
    }

    private long createRefusedHdcStatusTracking(long curfewId) {
        return offenderCurfewRepository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED);
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
