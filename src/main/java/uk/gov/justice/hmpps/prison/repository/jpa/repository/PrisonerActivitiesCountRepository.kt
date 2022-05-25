package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id

interface PrisonerActivitiesCountRepository : CrudRepository<PrisonerActivity, Long> {
  /*
   * Note that have to use START_TIME to calculate the time period rather than CS.SLOT_CATEGORY_CODE since there is no
   * guarantee that the latter is populated correctly and indeed in the production database is often PM / ED when the
   * start time is in the morning.
   * This query also restricts to prisoners with an active booking at any prison.
  */
  @Query(
    """
        SELECT 
          OPP.OFFENDER_BOOK_ID AS booking_id,
          OPP.SUSPENDED_FLAG AS suspended,
          CS.START_TIME as start_time
         FROM OFFENDER_PROGRAM_PROFILES OPP
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID 
          AND OB.ACTIVE_FLAG = 'Y' 
          AND OB.BOOKING_SEQ = 1
        INNER JOIN COURSE_ACTIVITIES CA ON CA.CRS_ACTY_ID = OPP.CRS_ACTY_ID
        INNER JOIN COURSE_SCHEDULES CS ON CA.CRS_ACTY_ID = CS.CRS_ACTY_ID
          AND CS.SCHEDULE_DATE >= OPP.OFFENDER_START_DATE
          AND CS.SCHEDULE_DATE <= COALESCE(OPP.OFFENDER_END_DATE, CA.SCHEDULE_END_DATE, CS.SCHEDULE_DATE)
          AND CS.SCHEDULE_DATE >= :startDate
          AND CS.SCHEDULE_DATE <= :endDate
        WHERE CA.AGY_LOC_ID = :agencyId 
          AND OPP.AGY_LOC_ID = :agencyId
          AND (OPP.OFFENDER_PROGRAM_STATUS = 'ALLOC'
             OR (OPP.OFFENDER_PROGRAM_STATUS = 'END' AND OPP.OFFENDER_END_DATE >= :endDate))
          AND CA.ACTIVE_FLAG = 'Y'
          AND CA.COURSE_ACTIVITY_TYPE IS NOT NULL
          AND CS.CATCH_UP_CRS_SCH_ID IS NULL
          AND (TO_CHAR(CS.SCHEDULE_DATE, 'DY'), CS.SLOT_CATEGORY_CODE) NOT IN
            (SELECT OE.EXCLUDE_DAY, COALESCE(OE.SLOT_CATEGORY_CODE, CS.SLOT_CATEGORY_CODE)
               FROM OFFENDER_EXCLUDE_ACTS_SCHDS OE
              WHERE OE.OFF_PRGREF_ID = OPP.OFF_PRGREF_ID)
  """,
    nativeQuery = true
  )
  fun getActivities(
    agencyId: String,
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<PrisonerActivity>
}

@Entity
data class PrisonerActivity(@Id val bookingId: Long, val suspended: String?, val startTime: LocalDateTime)

data class PrisonerActivitiesCount(val total: Long, val suspended: Long, val notRecorded: Long)
