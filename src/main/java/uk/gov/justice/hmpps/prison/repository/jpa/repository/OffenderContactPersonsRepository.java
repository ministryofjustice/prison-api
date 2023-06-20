package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderContactPerson;

import java.util.List;

public interface OffenderContactPersonsRepository extends CrudRepository<OffenderContactPerson, Long> {
    List<OffenderContactPerson> findAllByPersonIdAndOffenderBooking_BookingId(final long personId, final long bookingId);

    @EntityGraph(type = EntityGraphType.FETCH, value = "contacts-with-details")
    List<OffenderContactPerson> findAllByOffenderBooking_BookingIdOrderByIdDesc(final long bookingId);

    @EntityGraph(type = EntityGraphType.FETCH, value = "contacts-with-relationship")
    List<OffenderContactPerson> findAllByOffenderBooking_BookingIdAndPersonIdIn(final long bookingId, final List<Long> personIds);
}
