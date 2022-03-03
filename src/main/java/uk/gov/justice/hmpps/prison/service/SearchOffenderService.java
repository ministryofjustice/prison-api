package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingSearchRequest;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.support.InmatesHelper;
import uk.gov.justice.hmpps.prison.service.support.SearchOffenderRequest;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class SearchOffenderService {
    private final BookingService bookingService;
    private final UserService userService;
    private final InmateRepository repository;
    private final AuthenticationFacade securityUtils;
    private final Pattern offenderNoRegex;
    private final int maxBatchSize;

    public SearchOffenderService(final BookingService bookingService,
                                     final UserService userService,
                                     final InmateRepository repository,
                                     final AuthenticationFacade securityUtils,
                                     @Value("${api.offender.no.regex.pattern:^[A-Za-z]\\d{4}[A-Za-z]{2}$}") final String offenderNoRegex,
                                     @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.repository = repository;
        this.securityUtils = securityUtils;
        this.offenderNoRegex = Pattern.compile(offenderNoRegex);
        this.maxBatchSize = maxBatchSize;
    }

    public Page<OffenderBooking> findOffenders(final SearchOffenderRequest request) {
        Objects.requireNonNull(request.getLocationPrefix(), "locationPrefix is a required parameter");
        log.info("Searching for offenders, criteria: {}", request);

        final Set<String> caseloads = securityUtils.isOverrideRole() ? Set.of() : userService.getCaseLoadIds(request.getUsername());

        final var bookingsPage = getBookings(request, caseloads);
        final var bookings = bookingsPage.getItems();
        final var bookingIds = bookings.stream().map(OffenderBooking::getBookingId).toList();

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
        final PageRequest pageRequest = StringUtils.isBlank(request.getOrderBy()) ? request.toBuilder().orderBy(GlobalSearchService.DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT).build() : request;

        String offenderNo = null;
        String searchTerm1 = null;
        String searchTerm2 = null;

        // Search by keywords and values
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
                OffenderBookingSearchRequest.builder()
                        .caseloads(caseloads)
                        .offenderNo(offenderNo)
                        .searchTerm1(searchTerm1)
                        .searchTerm2(searchTerm2)
                        .locationPrefix(request.getLocationPrefix())
                        .alerts(request.getAlerts())
                        .convictedStatus(request.getConvictedStatus())
                        .fromDob(request.getFromDob())
                        .toDob(request.getToDob())
                        .pageRequest(pageRequest)
                        .build());


    }

    private boolean isOffenderNo(final String potentialOffenderNumber) {
        final var m = offenderNoRegex.matcher(potentialOffenderNumber);
        return m.find();
    }
}
