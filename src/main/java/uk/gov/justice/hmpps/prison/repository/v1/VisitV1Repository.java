package uk.gov.justice.hmpps.prison.repository.v1;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.RepositoryBase;
import uk.gov.justice.hmpps.prison.repository.v1.model.AvailableDatesSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.ContactPersonSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.UnavailabilityReasonSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.VisitSlotsSP;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ADULT_CNT;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CONTACT_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_DATES;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_DATE_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_FROM_DATE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_REASON_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROOT_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TO_DATE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_VISITOR_CNT;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetAvailableDates;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetContactList;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetUnavailability;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetVisitSlotsWithCapacity;

@SuppressWarnings("unchecked")
@Slf4j
@Repository
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class VisitV1Repository extends RepositoryBase {

    private final GetAvailableDates getAvailableDates;
    private final GetContactList getContactList;
    private final GetUnavailability getUnavailability;
    private final GetVisitSlotsWithCapacity getVisitSlotsWithCapacity;

    public List<AvailableDatesSP> getAvailableDates(final Long offenderId, final LocalDate fromDate, final LocalDate toDate) {

        final var params = new MapSqlParameterSource()
                .addValue(P_ROOT_OFFENDER_ID, offenderId)
                .addValue(P_FROM_DATE, fromDate)
                .addValue(P_TO_DATE, toDate);

        final var result = getAvailableDates.execute(params);

        return (List<AvailableDatesSP>) result.get(P_DATE_CSR);
    }

    public List<ContactPersonSP> getContactList(final Long offenderId) {

        final var params = new MapSqlParameterSource()
                .addValue(P_ROOT_OFFENDER_ID, offenderId);

        final var result = getContactList.execute(params);

        return (List<ContactPersonSP>) result.get(P_CONTACT_CSR);
    }

    public List<UnavailabilityReasonSP> getUnavailability(final Long offenderId, final String dates) {

        final var params = new MapSqlParameterSource()
                .addValue(P_ROOT_OFFENDER_ID, offenderId)
                .addValue(P_DATES, dates);

        final var result = getUnavailability.execute(params);

        return (List<UnavailabilityReasonSP>) result.get(P_REASON_CSR);
    }

    public List<VisitSlotsSP> getVisitSlotsWithCapacity(final String prisonId, final LocalDate fromDate, final LocalDate toDate) {

        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_FROM_DATE, fromDate)
                .addValue(P_TO_DATE, toDate)
                .addValue(P_VISITOR_CNT, 1)
                .addValue(P_ADULT_CNT, 1);

        final var result = getVisitSlotsWithCapacity.execute(params);

        return (List<VisitSlotsSP>) result.get(P_DATE_CSR);
    }
}
