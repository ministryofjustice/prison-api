package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OffenderBookingRepository extends
    PagingAndSortingRepository<OffenderBooking, Long>,
    JpaSpecificationExecutor<OffenderBooking>,
    CrudRepository<OffenderBooking, Long> {

    Optional<OffenderBooking> findByOffenderNomsIdAndActive(String nomsId, boolean active);

    Optional<OffenderBooking> findByOffenderNomsIdAndBookingSequence(String nomsId, Integer bookingSequence);

    @EntityGraph(type = EntityGraphType.FETCH, value = "booking-with-sentence-summary")
    Optional<OffenderBooking> findWithSentenceSummaryByOffenderNomsIdAndBookingSequence(String nomsId, Integer bookingSequence);

    /** Need a distinct here otherwise we will get the same offender bookings more than once as it does two left joins */
    Page<OffenderBookingId> findDistinctByActiveTrueAndLocationAndSentences_statusAndSentences_CalculationType_CalculationTypeNotLikeAndSentences_CalculationType_CategoryNot(
        AgencyLocation agencyLocation, String status, String calculationType, String category, Pageable pageable
    );

    /**
     * Separate version of the standard findAllById that uses the entity graph to load the data.  Paging should always
     * be split out when doing a FETCH otherwise hibernate has to load all the data in memory.
     */
    @EntityGraph(type = EntityGraphType.FETCH, value = "booking-with-sentence-summary")
    List<OffenderBooking> findAllByBookingIdIn(List<Long> bookingIds);

    @EntityGraph(type = EntityGraphType.FETCH, value = "booking-with-sentence-summary")
    List<OffenderBooking> findAllOffenderBookingsByActiveTrueAndOffenderNomsIdInAndSentences_statusAndSentences_CalculationType_CalculationTypeNotLikeAndSentences_CalculationType_CategoryNot(
        Set<String> nomsIds,
        String status,
        String calculationType, String category
    );

    Optional<OffenderBooking> findByBookingId(Long bookingId);

    @NotNull
    @Override
    @EntityGraph(type = EntityGraphType.FETCH, value = "booking-with-summary")
    Page<OffenderBooking> findAll(@NotNull Specification<OffenderBooking> filter, @NotNull Pageable pageable);
}
