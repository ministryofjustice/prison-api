package net.syscon.elite.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.SearchOffenderService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.support.InmatesHelper;
import net.syscon.elite.service.support.SearchOffenderRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class SearchOffenderServiceImpl implements SearchOffenderService {
    private final BookingService bookingService;
    private final UserService userService;
    private final InmateRepository repository;
    private final AuthenticationFacade securityUtils;
    private final String locationTypeGranularity;
    private final Pattern offenderNoRegex;
    private final int maxBatchSize;

    public SearchOffenderServiceImpl(final BookingService bookingService,
                                     final UserService userService,
                                     final InmateRepository repository,
                                     final AuthenticationFacade securityUtils,
                                     @Value("${api.users.me.locations.locationType:WING}") final String locationTypeGranularity,
                                     @Value("${api.offender.no.regex.pattern:^[A-Za-z]\\d{4}[A-Za-z]{2}$}") final String offenderNoRegex,
                                     @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.repository = repository;
        this.securityUtils = securityUtils;
        this.locationTypeGranularity = locationTypeGranularity;
        this.offenderNoRegex = Pattern.compile(offenderNoRegex);
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    public Page<OffenderBooking> findOffenders(final SearchOffenderRequest request) {
        Objects.requireNonNull(request.getLocationPrefix(), "locationPrefix is a required parameter");
        log.info("Searching for offenders, criteria: {}", request);

        final Set<String> caseloads = securityUtils.isOverrideRole() ? Set.of() : userService.getCaseLoadIds(request.getUsername());

        final var bookingsPage = getBookings(request, caseloads);
        final var bookings = bookingsPage.getItems();
        final var bookingIds = bookings.stream().map(OffenderBooking::getBookingId).collect(Collectors.toList());

        log.info("Searching for offenders, Found {} offenders, page size {}", bookingsPage.getTotalRecords(), bookingsPage.getItems().size());

        if (!CollectionUtils.isEmpty(bookingIds)) {
            if (request.isReturnIep()) {
                final var bookingIEPSummary = bookingService.getBookingIEPSummary(bookingIds, false);
                bookings.forEach(booking -> booking.setIepLevel(bookingIEPSummary.get(booking.getBookingId()).getIepLevel()));
            }
            if (request.isReturnAlerts()) {
                final var alertCodesForBookings = bookingService.getBookingAlertSummary(bookingIds, LocalDateTime.now());
                bookings.forEach(booking -> booking.setAlertsDetails(alertCodesForBookings.get(booking.getBookingId())));
            }
            if (request.isReturnCategory()) {
                final var batch = Lists.partition(bookingIds, maxBatchSize);
                batch.forEach(bookingIdList -> {
                    log.info("Searching for offenders, calling findAssessments with {} bookingIds and {} caseloads", bookingIdList.size(), caseloads.size());
                    final var assessmentsForBookings = repository.findAssessments(bookingIdList, "CATEGORY", caseloads);
                    InmatesHelper.setCategory(bookings, assessmentsForBookings);
                });
            }
        }

        return bookingsPage;
    }

    private Page<OffenderBooking> getBookings(final SearchOffenderRequest request, final Set<String> caseloads) {
        final var keywordSearch = StringUtils.upperCase(StringUtils.trimToEmpty(request.getKeywords()));
        final PageRequest pageRequest = StringUtils.isBlank(request.getOrderBy()) ? request.toBuilder().orderBy(DEFAULT_OFFENDER_SORT).build() : request;

        String offenderNo = null;
        String searchTerm1 = null;
        String searchTerm2 = null;

        if (StringUtils.isNotBlank(keywordSearch)) {
            if (isOffenderNo(keywordSearch)) {
                offenderNo = keywordSearch;
            } else {
                final var nameSplit = StringUtils.split(keywordSearch, " ,");
                searchTerm1 = nameSplit[0];

                if (nameSplit.length > 1) {
                    searchTerm2 = nameSplit[1];
                }
            }
        }


        return repository.searchForOffenderBookings(
                caseloads, offenderNo, searchTerm1, searchTerm2,
                request.getLocationPrefix(),
                request.getAlerts(),
                locationTypeGranularity, pageRequest);
    }

    private boolean isOffenderNo(final String potentialOffenderNumber) {
        final var m = offenderNoRegex.matcher(potentialOffenderNumber);
        return m.find();
    }
}
