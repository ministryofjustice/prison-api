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
        }).collect(Collectors.toList());

        final int count = repository.getAdjudicationCount(bookingId);

        return AdjudicationDetail.builder().awards(filteredList).adjudicationCount(count).build();
    }
}
