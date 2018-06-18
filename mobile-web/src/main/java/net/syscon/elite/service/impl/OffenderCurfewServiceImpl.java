package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseloadToAgencyMappingService;
import net.syscon.elite.service.OffenderCurfewService;
import net.syscon.elite.service.support.OffenderCurfew;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.*;

@Service
@Transactional(readOnly = true)
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
    private final Clock clock;

    public OffenderCurfewServiceImpl(
            OffenderCurfewRepository offenderCurfewRepository,
            CaseloadToAgencyMappingService caseloadToAgencyMappingService,
            BookingService bookingService,
            Clock clock) {
        this.offenderCurfewRepository = offenderCurfewRepository;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.bookingService = bookingService;
        this.clock = clock;
    }

    @Override
    public List<OffenderSentenceDetail> getHomeDetentionCurfewCandidates(String username) {

        final LocalDate earliestArdOrCrd = LocalDate.now(clock).plusDays(DAYS_TO_ADD);
        final List<OffenderSentenceDetail> offenderSentences = bookingService.getOffenderSentencesSummary(null, username, Collections.emptyList());
        final Collection<OffenderCurfew> curfews = offenderCurfewRepository.offenderCurfews(agencyIdsFor(username));

        return getHomeDetentionCurfewCandidates(curfews, earliestArdOrCrd, offenderSentences);
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
     * @param curfews
     * @return
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
            return
                (detail.getHomeDetentionCurfewEligibilityDate() != null) &&
                offendersWithoutCurfewApprovalStatus.contains(os.getBookingId()) &&
                (
                    isBeforeOrEqual(earliestArdOrCrd, detail.getAutomaticReleaseDate()) ||
                    isBeforeOrEqual(earliestArdOrCrd, detail.getConditionalReleaseDate())
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
