package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.AdjudicationDetail;
import net.syscon.elite.api.model.Award;
import net.syscon.elite.repository.AdjudicationsRepository;
import net.syscon.elite.service.AdjudicationService;
import net.syscon.elite.service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AdjudicationServiceImpl implements AdjudicationService {

    private final AdjudicationsRepository repository;
    private final BookingService bookingService;

    @Value("${api.cutoff.adjudication.months:3}") private int adjudicationCutoffDefault;
    @Value("${api.cutoff.award.months:0}") private int awardCutoffDefault;

    @Autowired
    public AdjudicationServiceImpl(AdjudicationsRepository adjudicationsRepository, BookingService bookingService) {
        this.repository = adjudicationsRepository;
        this.bookingService = bookingService;
    }

    /**
     * Get awards that have not expired, i.e. the end date is today or later, and
     * count proved adjudications which expired on or later than the from date.
     */
    @Override
    public AdjudicationDetail getAdjudications(long bookingId, LocalDate awardCutoffDate, LocalDate adjudicationCutoffDate) {

        bookingService.verifyBookingAccess(bookingId);
        final List<Award> list = repository.findAwards(bookingId);
        final LocalDate today = LocalDate.now();
        if (awardCutoffDate == null) {
            awardCutoffDate = today.plus(-awardCutoffDefault, ChronoUnit.MONTHS);
        }
        if (adjudicationCutoffDate == null) {
            adjudicationCutoffDate = today.plus(-adjudicationCutoffDefault, ChronoUnit.MONTHS);
        }
        final Iterator<Award> iterator = list.iterator();
        int adjudicationCount = 0;
        Award previous = null;

        while (iterator.hasNext()) {
            final Award current = iterator.next();
            final LocalDate endDate = calculateEndDate(current);

            if (!adjudicationCutoffDate.isAfter(endDate)) {
                if (changed(previous, current)) {
                    adjudicationCount++;
                    previous = current;
                }
            }
            if (awardCutoffDate.isAfter(endDate)) {
                iterator.remove();
            }
        }
        return AdjudicationDetail.builder().awards(list).adjudicationCount(adjudicationCount).build();
    }

    private LocalDate calculateEndDate(final Award award) {
        LocalDate endDate = award.getEffectiveDate();
        if (award.getMonths() != null) {
            endDate = endDate.plus(award.getMonths(), ChronoUnit.MONTHS);
        }
        if (award.getDays() != null) {
            endDate = endDate.plusDays(award.getDays());
        }
        return endDate;
    }

    private boolean changed(Award previous, Award current) {
        return previous == null || !Objects.equals(previous.getHearingId(), current.getHearingId());
        // Note we only consider the hearing id, not the sequence number as we only
        // expect at most one proved adjudication in the sequence list
    }
}
