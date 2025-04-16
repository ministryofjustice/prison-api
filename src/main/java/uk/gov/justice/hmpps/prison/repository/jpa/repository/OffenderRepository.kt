package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.jetbrains.annotations.NotNull
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.api.model.CorePersonRecordAlias
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import java.time.LocalDate
import java.util.Optional

@Repository
interface OffenderRepository : JpaRepository<Offender, Long> {
  fun findByLastNameAndFirstNameAndBirthDate(lastName: String, firstName: String, dob: LocalDate): List<Offender>

  /**
   * This method grabs the root offender, which is the first offender record created for the person.  This is not the
   * same as the current alias, which is determined by getting the offender record linked to the most recent booking
   * (bookingSequence of 1).
   */
  @Query("select o from Offender o left join fetch o.bookings b WHERE o.nomsId = :nomsId and o.id = o.rootOffenderId")
  fun findRootOffenderByNomsId(nomsId: String): Optional<Offender>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Offender o where o.nomsId = :nomsId and o.id = o.rootOffenderId")
  fun findRootOffenderByNomsIdForUpdate(@NotNull nomsId: String): Optional<Offender>

  @Query("select o from OffenderBooking b inner join Offender o on b.offender = o where o.nomsId = :nomsId and b.bookingSequence = 1")
  fun findLinkedToLatestBooking(@NotNull nomsId: String): Optional<Offender>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("select o from OffenderBooking b inner join Offender o on b.offender = o where o.nomsId = :nomsId and b.bookingSequence = 1")
  fun findLinkedToLatestBookingForUpdate(@NotNull nomsId: String): Optional<Offender>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("select o from Offender o where o.id = :offenderId")
  fun findByIdForUpdate(@NotNull offenderId: Long): Optional<Offender>

  @Query(
    """
    SELECT new uk.gov.justice.hmpps.prison.api.model.CorePersonRecordAlias(
        o.nomsId,
        o.id,
        ob.bookingId,
        o.firstName,
        o.middleName,
        o.middleName2,
        o.lastName,
        o.birthDate,
        nt.code,
        nt.description,
        t.code,
        t.description,
        s.code,
        s.description,
        e.code,
        e.description
    )
    FROM Offender o
    LEFT JOIN OffenderBooking ob
    ON o.id = ob.offender.id
    AND ob.bookingSequence = 1
    LEFT JOIN NameType nt ON nt.code = o.aliasNameType.code
    LEFT JOIN Title t ON t.code = o.title.code
    LEFT JOIN Gender s ON s.code = o.gender.code
    LEFT JOIN Ethnicity e ON e.code = o.ethnicity.code
    WHERE o.nomsId = :nomsId
    ORDER BY o.createDate DESC
    """,
  )
  fun findByNomsId(nomsId: String): List<CorePersonRecordAlias>
}
