package net.syscon.elite.service.v1;

import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.repository.v1.BookingV1Repository;
import net.syscon.elite.repository.v1.OffenderV1Repository;
import net.syscon.elite.service.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('NOMIS_API_V1')")
public class NomisApiV1Service {

    private final BookingV1Repository bookingV1Repository;
    private final OffenderV1Repository offenderV1Repository;

    public NomisApiV1Service(BookingV1Repository bookingV1Repository, OffenderV1Repository offenderV1Repository) {
        this.bookingV1Repository = bookingV1Repository;
        this.offenderV1Repository = offenderV1Repository;
    }


    public Location getLatestBookingLocation(final String nomsId) {
        return bookingV1Repository.getLatestBooking(nomsId)
                .map(l -> Location.builder()
                        .establishment(new CodeDescription(l.getAgyLocId(), l.getAgyLocDesc()))
                        .housingLocation(StringUtils.isNotBlank(l.getHousingLocation()) ? new InternalLocation(l.getHousingLocation(), l.getHousingLevels()) : null)
                        .build())
                .orElseThrow(EntityNotFoundException.withId(nomsId));
    }

    public Offender getOffender(final String nomsId) {
        return offenderV1Repository.getOffender(nomsId)
                .map(o -> Offender.builder()
                        .givenName(o.getFirstName())
                        .middleNames(o.getMiddleNames())
                        .surname(o.getLastName())
                        .birthDate(o.getBirthDate())
                        .build())
                .orElseThrow(EntityNotFoundException.withId(nomsId));
    }

    @Transactional
    public String createTransaction(String prisonId, String nomsId, String type, String description, BigDecimal amountInPounds, LocalDate txDate, String txId, String uniqueClientId) {

        return bookingV1Repository.postTransaction(prisonId, nomsId, type, description, amountInPounds, txDate, txId, uniqueClientId);
    }

    public Image getOffenderImage(final String nomsId) {
        return offenderV1Repository.getPhoto(nomsId).orElseThrow(EntityNotFoundException.withId(nomsId));
    }
}
