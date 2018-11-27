package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.SearchOffenderService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmatesHelper;
import net.syscon.elite.service.support.SearchOffenderRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class SearchOffenderServiceImpl implements SearchOffenderService {
    private final BookingService bookingService;
    private final UserService userService;
    private final InmateRepository repository;
    private final UserSecurityUtils securityUtils;
    private final String locationTypeGranularity;
    private final Pattern offenderNoRegex;

    public SearchOffenderServiceImpl(BookingService bookingService, UserService userService, InmateRepository repository, UserSecurityUtils securityUtils,
                                     @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity,
                                     @Value("${api.offender.no.regex.pattern:^[A-Za-z]\\d{4}[A-Za-z]{2}$}") String offenderNoRegex) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.repository = repository;
        this.securityUtils = securityUtils;
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

        log.info("Searching for offenders, criteria: {}", request);
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

        final Set<String> caseloads = securityUtils.isOverrideRole() ? Collections.emptySet() : userService.getCaseLoadIds(request.getUsername());

        final Page<OffenderBooking> bookingsPage = repository.searchForOffenderBookings(
                caseloads, offenderNo, searchTerm1, searchTerm2,
                request.getLocationPrefix(),
                request.getAlerts(),
                locationTypeGranularity, pageRequest);

        final List<OffenderBooking> bookings = bookingsPage.getItems();
        final List<Long> bookingIds = bookings.stream().map(OffenderBooking::getBookingId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(bookingIds)) {
            if (request.isReturnIep()) {
                final Map<Long, PrivilegeSummary> bookingIEPSummary = bookingService.getBookingIEPSummary(bookingIds, false);
                bookings.forEach(booking -> {
                    booking.setIepLevel(bookingIEPSummary.get(booking.getBookingId()).getIepLevel());
                });
            }
            if (request.isReturnAlerts()) {
                final Map<Long, List<String>> alertCodesForBookings = bookingService.getBookingAlertSummary(bookingIds, LocalDateTime.now());
                bookings.forEach(booking -> {
                    booking.setAlertsDetails(alertCodesForBookings.get(booking.getBookingId()));
                });
            }
            if (request.isReturnCategory()) {
                final List<AssessmentDto> assessmentsForBookings = repository.findAssessments(bookingIds, "CATEGORY", caseloads);
                InmatesHelper.setCategory(bookings, assessmentsForBookings);
            }
        }
        log.info("Found {} offenders, page size {}", bookingsPage.getTotalRecords(), bookingsPage.getItems().size());

        return bookingsPage;
    }

    private boolean isOffenderNo(String potentialOffenderNumber) {
        Matcher m = offenderNoRegex.matcher(potentialOffenderNumber);
        return m.find();
    }
}
