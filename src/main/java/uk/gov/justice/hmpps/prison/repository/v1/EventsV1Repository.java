package uk.gov.justice.hmpps.prison.repository.v1;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.RepositoryBase;
import uk.gov.justice.hmpps.prison.repository.v1.model.EventSP;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.GetEvents;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.P_EVENT_TYPE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.P_FROM_TS;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.P_LIMIT;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_EVENTS_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROOT_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_SINGLE_OFFENDER_ID;

@Repository
public class EventsV1Repository extends RepositoryBase {

    private final GetEvents getEventsProc;

    public EventsV1Repository(final GetEvents getEventsProc) {
        this.getEventsProc = getEventsProc;
    }

    public List<EventSP> getEvents(final String prisonId, final String nomsId, final Long rootOffenderId, final String singleOffenderId,
                                   final String eventType, final LocalDateTime fromDateTime, final Long limit) {

        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, rootOffenderId)
                .addValue(P_SINGLE_OFFENDER_ID, singleOffenderId)
                .addValue(P_EVENT_TYPE, eventType)
                .addValue(P_FROM_TS, fromDateTime)
                .addValue(P_LIMIT, limit);

        final var result = getEventsProc.execute(params);
        //noinspection unchecked
        return (List<EventSP>) result.get(P_EVENTS_CSR);
    }
}
