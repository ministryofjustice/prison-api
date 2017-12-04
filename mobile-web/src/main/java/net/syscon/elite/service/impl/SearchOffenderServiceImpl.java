package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.SearchOffenderService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.support.PageRequest;
import net.syscon.elite.service.support.SearchOffenderRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String keywordSearch = StringUtils.upperCase(StringUtils.trimToEmpty(request.getKeywords()));
        String offenderNo = null;
        String lastName = null;
        String firstName = null;

        if (StringUtils.isNotBlank(keywordSearch)) {
            if (isOffenderNo(keywordSearch)) {
                offenderNo = keywordSearch;
            } else {
                String [] nameSplit = StringUtils.splitByWholeSeparatorPreserveAllTokens(keywordSearch, ",");
                lastName = nameSplit[0];

                if (nameSplit.length > 1) {
                    firstName = nameSplit[1];
                }
            }
        }

        Set<String> userCaseLoadIds = userService.getCaseLoadIds(request.getUsername());

        PageRequest pageRequest;

        if (StringUtils.isBlank(request.getOrderBy())) {
            pageRequest = request.toBuilder().orderBy(DEFAULT_OFFENDER_SORT).build();
        } else {
            pageRequest = request;
        }

        Page<OffenderBooking> bookings = repository.searchForOffenderBookings(
                userCaseLoadIds, offenderNo, lastName, firstName,
                StringUtils.replaceAll(request.getLocationPrefix(), "_", ""),
                locationTypeGranularity, pageRequest);

        bookings.getItems().forEach(booking -> {
            PrivilegeSummary iepSummary = bookingService.getBookingIEPSummary(booking.getBookingId(), false);

            booking.setIepLevel(iepSummary.getIepLevel());
        });

        return bookings;
    }

    private boolean isOffenderNo(String potentialOffenderNumber) {
        Matcher m = offenderNoRegex.matcher(potentialOffenderNumber);
        return m.find();
    }
}
