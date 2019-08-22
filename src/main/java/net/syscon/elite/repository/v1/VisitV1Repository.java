package net.syscon.elite.repository.v1;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.AvailableDatesSP;
import net.syscon.elite.repository.v1.model.ContactPersonSP;
import net.syscon.elite.repository.v1.model.UnavailabilityReasonSP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;
import static net.syscon.elite.repository.v1.storedprocs.VisitsProc.*;

@Slf4j
@Repository
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class VisitV1Repository extends RepositoryBase {

    private final GetAvailableDates getAvailableDates;
    private final GetContactList getContactList;
    private final GetUnavailability getUnavailability;

    public List<AvailableDatesSP> getAvailableDates(String offenderId, LocalDate fromDate, LocalDate toDate) {

        final var params = new MapSqlParameterSource()
                .addValue(P_ROOT_OFFENDER_ID, offenderId)
                .addValue(P_FROM_DATE, fromDate)
                .addValue(P_TO_DATE, toDate);

        final var result = getAvailableDates.execute(params);

        return (List<AvailableDatesSP>) result.get(P_DATE_CSR);
    }

    public List<ContactPersonSP> getContactList(String offenderId) {

        final var params = new MapSqlParameterSource()
                .addValue(P_ROOT_OFFENDER_ID, offenderId);

        final var result = getContactList.execute(params);

        return (List<ContactPersonSP>) result.get(P_CONTACT_CSR);
    }


    public List<UnavailabilityReasonSP> getUnavailability(Long offenderId, String dates) {

        final var params = new MapSqlParameterSource()
                .addValue(P_ROOT_OFFENDER_ID, offenderId)
                .addValue(P_DATES, dates);

        final var result = getUnavailability.execute(params);

        return (List<UnavailabilityReasonSP>) result.get(P_REASON_CSR);
    }
}
