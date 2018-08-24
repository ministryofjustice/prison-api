package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.SearchOffenderService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.support.SearchOffenderRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SearchOffenderServiceImpl implements SearchOffenderService {
    private final BookingService bookingService;
    private final UserService userService;
    private final InmateRepository repository;
    private final String locationTypeGranularity;
    private final Pattern offenderNoRegex;

    public SearchOffenderServiceImpl(BookingService bookingService, UserService userService, InmateRepository repository,
                                     @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity,
                                     @Value("${api.offender.no.regex.pattern:^[A-Za-z]\\d{4}[A-Za-z]{2}$}") String offenderNoRegex) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.repository = repository;
        this.locationTypeGranularity = locationTypeGranularity;
        this.offenderNoRegex = Pattern.compile(offenderNoRegex);
    }

    @Override
    public Page<OffenderBooking> findOffenders(SearchOffenderRequest request) {
        Objects.requireNonNull(request.getLocationPrefix(), "locationPrefix is a required parameter");
        String keywordSearch = StringUtils.upperCase(StringUtils.trimToEmpty(request.getKeywords()));
        String offenderNo = null;
        String searchTerm1 = null;
        String searchTerm2 = null;

        if (StringUtils.isNotBlank(keywordSearch)) {
            if (isOffenderNo(keywordSearch)) {
                offenderNo = keywordSearch;
            } else {
                String [] nameSplit = StringUtils.split(keywordSearch, " ,");
                searchTerm1 = nameSplit[0];

                if (nameSplit.length > 1) {
                    searchTerm2 = nameSplit[1];
                }
            }
        }

        PageRequest pageRequest;

        if (StringUtils.isBlank(request.getOrderBy())) {
            pageRequest = request.toBuilder().orderBy(DEFAULT_OFFENDER_SORT).build();
        } else {
            pageRequest = request;
        }

        final Set<String> caseloads = bookingService.isOverrideRole() ? Collections.emptySet() : userService.getCaseLoadIds(request.getUsername());

        final Page<OffenderBooking> bookings = repository.searchForOffenderBookings(
                caseloads, offenderNo, searchTerm1, searchTerm2,
                request.getLocationPrefix(),
                request.getAlerts(),
                locationTypeGranularity, pageRequest);

        final List<Long> bookingIds = bookings.getItems().stream().map(OffenderBooking::getBookingId).collect(Collectors.toList());
        final Map<Long, PrivilegeSummary> bookingIEPSummary = bookingService.getBookingIEPSummary(bookingIds, false);
        final Map<Long, List<String>> alertCodesForBookings = repository.getAlertCodesForBookings(bookingIds, LocalDateTime.now());
        bookings.getItems().forEach(booking -> {
            booking.setIepLevel(bookingIEPSummary.get(booking.getBookingId()).getIepLevel());
            booking.setAlertsDetails(alertCodesForBookings.get(booking.getBookingId()));
        });
        return bookings;
    }

    private boolean isOffenderNo(String potentialOffenderNumber) {
        Matcher m = offenderNoRegex.matcher(potentialOffenderNumber);
        return m.find();
    }
}
