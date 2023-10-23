package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import org.jetbrains.annotations.NotNull
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface OffenderRepository : JpaRepository<Offender, Long> {
  fun findByNomsId(nomsId: String): List<Offender>

  fun findByLastNameAndFirstNameAndBirthDate(lastName: String, firstName: String, dob: LocalDate): List<Offender>

  @Query("select o from Offender o left join fetch o.bookings b WHERE o.nomsId = :nomsId order by b.bookingSequence asc")
  fun findOffendersByNomsId(@Param("nomsId") nomsId: String, pageable: Pageable): List<Offender>

  fun findOffenderByNomsId(nomsId: String): Optional<Offender> {
    return findOffendersByNomsId(nomsId, PageRequest.of(0, 1)).stream().findFirst()
  }

  @Query("select o from Offender o left join fetch o.bookings b WHERE o.nomsId = :nomsId and b.bookingSequence = 1")
  fun findOffenderWithLatestBookingByNomsId(@Param("nomsId") nomsId: String): Optional<Offender>

  @Query(value = "select o from Offender o join o.bookings ob join ob.images oi WHERE oi.captureDateTime > :start")
  fun getOffendersWithImagesCapturedAfter(@Param("start")start: LocalDateTime, pageable: Pageable): Page<Offender>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o from Offender o left join fetch o.bookings where o.nomsId = :nomsId and o.id = o.rootOffenderId")
  fun findOffenderByNomsIdOrNullForUpdate(@NotNull nomsId: String): Optional<Offender>
}
