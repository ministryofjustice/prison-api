package uk.gov.justice.hmpps.prison.service.curfews;

import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;
import uk.gov.justice.hmpps.prison.api.model.BaseSentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.HdcChecks;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalc;
import uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.CaseloadToAgencyMappingService;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService;
import uk.gov.justice.hmpps.prison.service.support.OffenderCurfew;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional(readOnly = true)
@Validated
public class OffenderCurfewService {

    private static final String HDC_APPROVE_DOMAIN = "HDC_APPROVE";
    private static final String HDC_REJECT_REASON_DOMAIN = "HDC_REJ_RSN";
    /**
     * Comparator for sorting OffenderSentenceCalc instances by HDCED (HomeDetentionCurfewEligibilityDate). Nulls sort high.
     */
    static final Comparator<OffenderSentenceCalc<? extends BaseSentenceCalcDates>> OSC_BY_HDCED_COMPARATOR =
            comparing(
                    OffenderSentenceCalc::getSentenceDetail,
                    comparing(bsd -> bsd == null ? null : bsd.getHomeDetentionCurfewEligibilityDate(),
                            nullsLast(naturalOrder())
                    )
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

    public OffenderCurfewService(
            final OffenderCurfewRepository offenderCurfewRepository,
            final CaseloadToAgencyMappingService caseloadToAgencyMappingService,
            final BookingService bookingService,
            final ReferenceDomainService referenceDomainService) {
        this.offenderCurfewRepository = offenderCurfewRepository;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
    }

    public List<OffenderSentenceCalc<BaseSentenceCalcDates>> getHomeDetentionCurfewCandidates(final String username) {

        final var agencyIds = agencyIdsFor(username);

        final var offenderBookingIdsForNewHDCProcess =
                curfewBookingIds(
                        currentOffenderCurfews(
                                offenderCurfewRepository.offenderCurfews(agencyIds)));

        return bookingService.getOffenderSentenceCalculationsForAgency(agencyIds)
                .stream()
                .filter(offenderSentenceCalculation -> (offenderSentenceCalculation.getHomeDetCurfEligibilityDate() != null) &&
                        offenderBookingIdsForNewHDCProcess.contains(offenderSentenceCalculation.getBookingId()))
                .map(os -> OffenderSentenceCalc.builder()
                        .bookingId(os.getBookingId())
                        .offenderNo(os.getOffenderNo())
                        .firstName(os.getFirstName())
                        .lastName(os.getLastName())
                        .agencyLocationId(os.getAgencyLocationId())
                        .sentenceDetail(BaseSentenceCalcDates.builder()
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
                .sorted(OSC_BY_HDCED_COMPARATOR)
                .collect(Collectors.toList());
    }

    private Set<Long> curfewBookingIds(Stream<OffenderCurfew> ocs) {
        return ocs.map(OffenderCurfew::getOffenderBookId).collect(toSet());
    }

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public HomeDetentionCurfew getLatestHomeDetentionCurfew(final long bookingId) {
        return offenderCurfewRepository.getLatestHomeDetentionCurfew(bookingId, StatusTrackingCodes.REFUSED_REASON_CODES)
                .orElseThrow(() -> new EntityNotFoundException("No 'latest' Home Detention Curfew found for bookingId " + bookingId));
    }

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public List<HomeDetentionCurfew> getBatchLatestHomeDetentionCurfew(final List<Long> bookingIds) {
        return offenderCurfewRepository.getBatchLatestHomeDetentionCurfew(bookingIds, StatusTrackingCodes.REFUSED_REASON_CODES);
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_USER') and hasAuthority('SCOPE_write')")
    public void setHdcChecks(final long bookingId, @Valid final HdcChecks hdcChecks) {
        withCurrentCurfewState(bookingId).setHdcChecks(hdcChecks);
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_USER') and hasAuthority('SCOPE_write')")
    public void deleteHdcChecks(Long bookingId) {
        withCurrentCurfewState(bookingId).deleteHdcChecks();
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_USER') and hasAuthority('SCOPE_write')")
    public void setApprovalStatus(final long bookingId, @Valid final ApprovalStatus approvalStatus) {

        if (!referenceDomainService.isReferenceCodeActive(HDC_APPROVE_DOMAIN, approvalStatus.getApprovalStatus())) {
            throw new IllegalArgumentException(String.format("Approval status code '%1$s' is not a valid NOMIS value.", approvalStatus.getApprovalStatus()));
        }
        final var refusedReason = approvalStatus.getRefusedReason();
        if (refusedReason != null) {
            if (!referenceDomainService.isReferenceCodeActive(HDC_REJECT_REASON_DOMAIN, refusedReason)) {
                throw new IllegalArgumentException(String.format("Refused reason code '%1$s' is not a valid NOMIS value.", refusedReason));
            }
        }

        withCurrentCurfewState(bookingId).setApprovalStatus(approvalStatus);
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_USER') and hasAuthority('SCOPE_write')")
    public void deleteApprovalStatus(Long bookingId) {
        withCurrentCurfewState(bookingId).deleteApprovalStatus();
    }

    private CurfewState withCurrentCurfewState(long bookingId) {
        val currentCurfew = getLatestHomeDetentionCurfew(bookingId);

        return CurfewState
                .getState(currentCurfew)
                .with(new CurfewActions(currentCurfew.getId(), offenderCurfewRepository));
    }


    private Set<String> agencyIdsFor(final String username) {
        return caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(username)
                .stream()
                .map(Agency::getAgencyId)
                .collect(toSet());
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
}
