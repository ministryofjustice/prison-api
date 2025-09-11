package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.Nullable;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OffenderCaseNoteRepository extends
    PagingAndSortingRepository<OffenderCaseNote, Long>,
    CrudRepository<OffenderCaseNote, Long>,
    JpaSpecificationExecutor<OffenderCaseNote> {

    Optional<OffenderCaseNote> findByIdAndOffenderBooking_BookingId(final Long id, final Long bookingId);

    @Query(value = "select new uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerCaseNoteTypeAndSubType(cn.bookingId, cn.typeCode, cn.subTypeCode, cn.occurrenceDateTime) from OffenderCaseNote cn where cn.bookingId in (:bookingIds) and cn.typeCode in (:types) and cn.occurrenceDate >= :cutoffDate")
    List<PrisonerCaseNoteTypeAndSubType> findCaseNoteTypesByBookingAndDate(
        List<Long> bookingIds, List<String> types, LocalDate cutoffDate
    );

    @NotNull
    @EntityGraph(type = EntityGraphType.FETCH, value = "case-note-with-author")
    Page<OffenderCaseNote> findAll(@Nullable Specification<OffenderCaseNote> spec, @NotNull Pageable pageable);
}

