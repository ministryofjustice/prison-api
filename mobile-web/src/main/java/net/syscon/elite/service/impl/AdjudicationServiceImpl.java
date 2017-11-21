package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.AdjudicationDetail;
import net.syscon.elite.api.model.Award;
import net.syscon.elite.repository.AdjudicationsRepository;
import net.syscon.elite.service.AdjudicationService;
import net.syscon.elite.service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdjudicationServiceImpl implements AdjudicationService {

    private final AdjudicationsRepository repository;
    private final BookingService bookingService;

    @Autowired
    public AdjudicationServiceImpl(AdjudicationsRepository adjudicationsRepository, BookingService bookingService) {
        this.repository = adjudicationsRepository;
        this.bookingService = bookingService;
    }

    @Override
    public AdjudicationDetail getAdjudications(final long bookingId, final LocalDate fromDate) {
        bookingService.verifyBookingAccess(bookingId);
        final List<Award> list = repository.findAwards(bookingId);

        final AtomicInteger i = new AtomicInteger(0);
        final AtomicReference<Award> previous = new AtomicReference<>();

        final List<Award> filteredList = list.stream().filter(t -> {
            if (fromDate == null) {
                return true;
            }
            LocalDate endDate = t.getEffectiveDate();
            if (t.getMonths() != null) {
                endDate = endDate.plus(t.getMonths(), ChronoUnit.MONTHS);
            }
            if (t.getDays() != null) {
                endDate = endDate.plusDays(t.getDays());
            }
            return fromDate.isEqual(endDate) || fromDate.isBefore(endDate);
        }).filter(t -> {
            // Note this assumes data is sorted
            if (changed(previous, t)) {
                previous.set(t);
                i.incrementAndGet();
            }
            return true;
        }).collect(Collectors.toList());

        return AdjudicationDetail.builder().awards(filteredList).adjudicationCount(i.get()).build();
    }

    private boolean changed(AtomicReference<Award> previous, Award current) {
        return previous.get() == null //
                || !Objects.equals(previous.get().getHearingId(), current.getHearingId())
                || !Objects.equals(previous.get().getHearingSequence(), current.getHearingSequence());
        // TODO do we consider h.result_seq for distinctness- Waiting on Viny for example data
    }
}
