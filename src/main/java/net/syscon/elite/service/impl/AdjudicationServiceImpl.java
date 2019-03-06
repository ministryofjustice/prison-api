package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.AdjudicationDetail;
import net.syscon.elite.api.model.Award;
import net.syscon.elite.repository.AdjudicationsRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.AdjudicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AdjudicationServiceImpl implements AdjudicationService {

    private final AdjudicationsRepository repository;

    @Value("${api.cutoff.adjudication.months:3}") private int adjudicationCutoffDefault;
    @Value("${api.cutoff.award.months:0}") private int awardCutoffDefault;

    @Autowired
    public AdjudicationServiceImpl(final AdjudicationsRepository adjudicationsRepository) {
        this.repository = adjudicationsRepository;
    }

    /**
     * Get awards that have not expired, i.e. the end date is today or later, and
     * count proved adjudications which expired on or later than the from date.
     */
    @Override
    @VerifyBookingAccess
    public AdjudicationDetail getAdjudications(final Long bookingId, final LocalDate awardCutoffDateParam, final LocalDate adjudicationCutoffDateParam) {
        final var list = repository.findAwards(bookingId);
        final var today = LocalDate.now();
        var awardCutoffDate = awardCutoffDateParam;
        if (awardCutoffDate == null) {
            awardCutoffDate = today.plus(-awardCutoffDefault, ChronoUnit.MONTHS);
        }
        var adjudicationCutoffDate = adjudicationCutoffDateParam;
        if (adjudicationCutoffDate == null) {
            adjudicationCutoffDate = today.plus(-adjudicationCutoffDefault, ChronoUnit.MONTHS);
        }
        final var iterator = list.iterator();
        var adjudicationCount = 0;
        Award previous = null;

        while (iterator.hasNext()) {
            final var current = iterator.next();
            final var endDate = calculateEndDate(current);

            if (!adjudicationCutoffDate.isAfter(endDate) && changed(previous, current)) {
                adjudicationCount++;
                previous = current;
            }
            if (awardCutoffDate.isAfter(endDate)) {
                iterator.remove();
            }
        }
        return AdjudicationDetail.builder().awards(list).adjudicationCount(adjudicationCount).build();
    }

    private LocalDate calculateEndDate(final Award award) {
        var endDate = award.getEffectiveDate();
        if (award.getMonths() != null) {
            endDate = endDate.plus(award.getMonths(), ChronoUnit.MONTHS);
        }
        if (award.getDays() != null) {
            endDate = endDate.plusDays(award.getDays());
        }
        return endDate;
    }

    private boolean changed(final Award previous, final Award current) {
        return previous == null || !Objects.equals(previous.getHearingId(), current.getHearingId());
        // Note we only consider the hearing id, not the sequence number as we only
        // expect at most one proved adjudication in the sequence list
    }
}
