package net.syscon.elite.service.v1;

import net.syscon.elite.api.model.v1.CodeDescription;
import net.syscon.elite.api.model.v1.InternalLocation;
import net.syscon.elite.api.model.v1.Location;
import net.syscon.elite.api.model.v1.Offender;
import net.syscon.elite.repository.v1.NomisApiV1Repository;
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

    private final NomisApiV1Repository dao;

    public NomisApiV1Service(NomisApiV1Repository dao) {
        this.dao = dao;
    }

    public Location getLatestBookingLocation(final String nomsId) {
        return dao.getLatestBooking(nomsId)
                .map(l -> Location.builder()
                        .establishment(new CodeDescription(l.getAgyLocId(), l.getAgyLocDesc()))
                        .housingLocation(StringUtils.isNotBlank(l.getHousingLocation()) ? new InternalLocation(l.getHousingLocation(), l.getHousingLevels()) : null)
                        .build())
                .orElseThrow(EntityNotFoundException.withId(nomsId));
    }

    public Offender getOffender(final String nomsId) {
        return dao.getOffender(nomsId)
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

        return dao.postTransaction(prisonId, nomsId, type, description, amountInPounds, txDate, txId, uniqueClientId);
    }
}
