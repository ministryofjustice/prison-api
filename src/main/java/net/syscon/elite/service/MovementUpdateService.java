package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderBooking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class MovementUpdateService {

    // TODO DT-235 Implement this service - currently only exists to mock in the API tests
    public OffenderBooking moveToCell(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime dateTime) {
        return null;
    }

}
