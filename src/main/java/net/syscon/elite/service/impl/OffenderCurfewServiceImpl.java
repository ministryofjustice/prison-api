package net.syscon.elite.service.impl;

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

    /**
     * Comparator for sorting OffenderCurfew instances by HDCED (HomeDetentionCurfewEligibilityDate). Nulls sort high.
     */
    private static final Comparator<OffenderSentenceDetail> HDCED_COMPARATOR =
            comparing(
                    osd -> osd.getSentenceDetail().getHomeDetentionCurfewEligibilityDate(),
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
            OffenderCurfewRepository offenderCurfewRepository,
            CaseloadToAgencyMappingService caseloadToAgencyMappingService,
            BookingService bookingService,
            ReferenceDomainService referenceDomainService,
            Clock clock) {
        this.offenderCurfewRepository = offenderCurfewRepository;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
        this.clock = clock;
    }

    @Override
    public List<OffenderSentenceDetail> getHomeDetentionCurfewCandidates(String username, boolean newVersion) {

        var earliestArdOrCrd = LocalDate.now(clock).plusDays(DAYS_TO_ADD);
        Set<String> agencyIds = agencyIdsFor(username);
        var curfews = offenderCurfewRepository.offenderCurfews(agencyIds);
        List<OffenderSentenceDetail> offenderSentences;
        if (newVersion) {
            var offenderSentenceCalculationsForAgency = bookingService.getOffenderSentenceCalculationsForAgency(agencyIds);

            offenderSentences = offenderSentenceCalculationsForAgency.stream()
                    .map(os -> OffenderSentenceDetail.builder()
                            .bookingId(os.getBookingId())
                            .offenderNo(os.getOffenderNo())
                            .sentenceDetail(SentenceDetail.builder()
                                    .bookingId(os.getBookingId())
                                    .sentenceExpiryDate(os.getSentenceExpiryDate())
                                    .homeDetentionCurfewEligibilityDate(os.getHomeDetCurfEligibilityDate())
                                    .homeDetentionCurfewActualDate(os.getHomeDetCurfActualDate())
                                    .automaticReleaseDate(os.getArd())
                                    .conditionalReleaseDate(os.getCrd())
                                    .nonParoleDate(os.getNpd())
                                    .postRecallReleaseDate(os.getPrrd())
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
                            .firstName(os.getFirstName())
                            .lastName(os.getLastName())
                            .agencyLocationId(os.getAgencyLocationId())
                            .build())
            .collect(Collectors.toList());

        } else {
            offenderSentences = bookingService.getOffenderSentencesSummary(null, username, Collections.emptyList());
        }

        return getHomeDetentionCurfewCandidates(curfews, earliestArdOrCrd, offenderSentences);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('SYSTEM_USER')")
    public void setHdcChecks(long bookingId, HdcChecks hdcChecks) {
        offenderCurfewRepository.setHDCChecksPassed(bookingId, hdcChecks);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('SYSTEM_USER')")
    public void setApprovalStatus(long bookingId, ApprovalStatus approvalStatus) {
        if (!referenceDomainService.isReferenceCodeActive("HDC_APPROVE", approvalStatus.getApprovalStatus())) {
            throw new EntityNotFoundException(String.format("Approval status code '%1$s' not found and active.",  approvalStatus));
        }
        offenderCurfewRepository.setApprovalStatusForLatestCurfew(bookingId, approvalStatus);
    }

    private Set<String> agencyIdsFor(String username) {
        return caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(username)
                .stream()
                .map(Agency::getAgencyId)
                .collect(toSet());
    }

    static List<OffenderSentenceDetail> getHomeDetentionCurfewCandidates(
            Collection<OffenderCurfew> curfews,
            LocalDate earliestArdOrCrd,
            List<OffenderSentenceDetail> offenderSentences) {

        final Set<Long> offendersLackingCurfewApprovalStatus = offendersLackingCurfewApprovalStatus(currentOffenderCurfews(curfews));

        return offenderSentences
                .stream()
                .filter(offenderIsEligibleForHomeCurfew(offendersLackingCurfewApprovalStatus, earliestArdOrCrd))
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
    static Stream<OffenderCurfew> currentOffenderCurfews(Collection<OffenderCurfew> curfews) {

        Map<Long, Optional<OffenderCurfew>> currentByOffenderBookdId = curfews
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

    static Set<Long> offendersLackingCurfewApprovalStatus(Stream<OffenderCurfew> currentOffenderCurfews) {
        return currentOffenderCurfews
                .filter(oc -> oc.getApprovalStatus() == null)
                .map(OffenderCurfew::getOffenderBookId)
                .collect(toSet());
    }

    static Predicate<OffenderSentenceDetail> offenderIsEligibleForHomeCurfew(
            Set<Long> offendersWithoutCurfewApprovalStatus,
            LocalDate earliestArdOrCrd) {

        return (OffenderSentenceDetail os) -> {
            final SentenceDetail detail = os.getSentenceDetail();
            final LocalDate ard = detail.getAutomaticReleaseOverrideDate() == null ? detail.getAutomaticReleaseDate() : detail.getAutomaticReleaseOverrideDate();
            final LocalDate crd = detail.getConditionalReleaseOverrideDate()  == null ? detail.getConditionalReleaseDate() : detail.getConditionalReleaseOverrideDate();
            return
                (detail.getHomeDetentionCurfewEligibilityDate() != null) &&
                offendersWithoutCurfewApprovalStatus.contains(os.getBookingId()) &&
                (
                    isBeforeOrEqual(earliestArdOrCrd, ard) || isBeforeOrEqual(earliestArdOrCrd, crd)
                );
        };
    }

    private static boolean isBeforeOrEqual(LocalDate d1, LocalDate d2) {
        return
            d2 != null &&
            (
                d1.isBefore(d2) ||
                d1.isEqual(d2)
            );
    }
}
