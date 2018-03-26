package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.service.AssignmentService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {
    private final UserService userService;
    private final InmateRepository inmateRepository;
    private final BookingService bookingService;
    private final String locationTypeGranularity;

    public AssignmentServiceImpl(InmateRepository inmateRepository, UserService userService,
                                 BookingService bookingService,
                                 @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity) {
        this.userService = userService;
        this.inmateRepository = inmateRepository;
        this.bookingService = bookingService;
        this.locationTypeGranularity = locationTypeGranularity;
    }

    @Override
    public Page<OffenderBooking> findMyAssignments(String username, long offset, long limit) {
        UserDetail loggedInUser = userService.getUserByUsername(username);

        Page<OffenderBooking> bookings = inmateRepository.findMyAssignments(
                loggedInUser.getStaffId(),
                loggedInUser.getActiveCaseLoadId(),
                locationTypeGranularity,
                DEFAULT_OFFENDER_SORT,
                true,
                offset,
                limit);

        List<Long> bookingIds = bookings.getItems().stream().map(OffenderBooking::getBookingId).collect(Collectors.toList());
        Map<Long, PrivilegeSummary> bookingIEPSummary = bookingService.getBookingIEPSummary(bookingIds, false);
        bookings.getItems().forEach(booking -> booking.setIepLevel(bookingIEPSummary.get(booking.getBookingId()).getIepLevel()));
        return bookings;
    }
}
