package uk.gov.justice.hmpps.prison.repository

import org.springframework.jdbc.core.SqlParameterValue
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule
import uk.gov.justice.hmpps.prison.api.model.PrisonerScheduleDto
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper
import uk.gov.justice.hmpps.prison.repository.sql.ScheduleRepositorySql
import uk.gov.justice.hmpps.prison.util.DateTimeConverter
import java.sql.Types
import java.time.LocalDate
import java.util.Objects

@Repository
class ScheduleRepository : RepositoryBase() {
  fun getAllActivitiesAtAgency(
    agencyId: String?,
    fromDate: LocalDate?,
    toDate: LocalDate?,
    orderByFields: String?,
    order: Order?,
    includeSuspended: Boolean,
    onlySuspended: Boolean,
  ): List<PrisonerSchedule> {
    val initialSql = ScheduleRepositorySql.GET_ALL_ACTIVITIES_AT_AGENCY.sql
    val sql = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.fieldMap)
      .addOrderBy(order, orderByFields)
      .build()
    val suspended =
      if (onlySuspended) {
        setOf("Y")
      } else if (includeSuspended) {
        setOf("Y", "N")
      } else {
        setOf("N")
      }

    val schedules = jdbcTemplate.query(
      sql,
      createParams(
        "agencyId",
        agencyId,
        "fromDate",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
        "toDate",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
        "includeSuspended",
        suspended,
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.filter { it.programHasntEnded() }.toPrisonerSchedules()
  }

  fun getActivitiesAtLocation(
    locationId: Long?,
    fromDate: LocalDate?,
    toDate: LocalDate?,
    orderByFields: String?,
    order: Order?,
    includeSuspended: Boolean,
  ): List<PrisonerSchedule> {
    val initialSql = ScheduleRepositorySql.GET_ACTIVITIES_AT_ONE_LOCATION.sql
    val builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.fieldMap)
    val sql = builder.addOrderBy(order, orderByFields).build()
    val schedules = jdbcTemplate.query(
      sql,
      createParams(
        "locationId",
        locationId,
        "fromDate",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
        "toDate",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
        "includeSuspended",
        if (includeSuspended) setOf("Y", "N") else setOf("N"),
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.toPrisonerSchedules()
  }

  fun getLocationAppointments(
    locationId: Long?,
    fromDate: LocalDate,
    toDate: LocalDate,
    orderByFields: String?,
    order: Order?,
  ): List<PrisonerSchedule> {
    Objects.requireNonNull(locationId, "locationId is a required parameter")
    val initialSql = ScheduleRepositorySql.GET_APPOINTMENTS_AT_LOCATION.sql
    return getScheduledEvents(initialSql, locationId, fromDate, toDate, orderByFields, order)
  }

  fun getLocationVisits(
    locationId: Long?,
    fromDate: LocalDate,
    toDate: LocalDate,
    orderByFields: String?,
    order: Order?,
  ): List<PrisonerSchedule> {
    Objects.requireNonNull(locationId, "locationId is a required parameter")
    val initialSql = ScheduleRepositorySql.GET_VISITS_AT_LOCATION.sql
    return getScheduledEvents(initialSql, locationId, fromDate, toDate, orderByFields, order)
  }

  private fun getScheduledEvents(
    initialSql: String,
    locationId: Long?,
    fromDate: LocalDate,
    toDate: LocalDate,
    orderByFields: String?,
    order: Order?,
  ): List<PrisonerSchedule> {
    val builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.fieldMap)
    val sql = builder.addOrderBy(order, orderByFields).build()
    val schedules = jdbcTemplate.query(
      sql,
      createParams(
        "locationId",
        locationId,
        "fromDate",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
        "toDate",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.toPrisonerSchedules()
  }

  fun getVisits(agencyId: String?, offenderNo: List<String>?, date: LocalDate?): List<PrisonerSchedule> {
    val schedules = jdbcTemplate.query(
      ScheduleRepositorySql.GET_VISITS.sql + ScheduleRepositorySql.AND_OFFENDER_NUMBERS.sql,
      createParams(
        "offenderNos",
        offenderNo,
        "date",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date)),
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.toPrisonerSchedules()
  }

  fun getAppointments(agencyId: String?, offenderNo: List<String>?, date: LocalDate?): List<PrisonerSchedule> {
    val schedules = jdbcTemplate.query(
      ScheduleRepositorySql.GET_APPOINTMENTS.sql + ScheduleRepositorySql.AND_OFFENDER_NUMBERS.sql,
      createParams(
        "offenderNos",
        offenderNo,
        "date",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date)),
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.toPrisonerSchedules()
  }

  fun getActivities(agencyId: String?, offenderNumbers: List<String>?, date: LocalDate?): List<PrisonerSchedule> {
    val schedules = jdbcTemplate.query(
      ScheduleRepositorySql.GET_ACTIVITIES.sql + ScheduleRepositorySql.AND_OFFENDER_NUMBERS.sql,
      createParams(
        "offenderNos",
        offenderNumbers,
        "date",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date)),
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.toPrisonerSchedules()
  }

  fun getCourtEvents(offenderNumbers: List<String>?, date: LocalDate?): List<PrisonerSchedule> {
    val schedules = jdbcTemplate.query(
      ScheduleRepositorySql.GET_COURT_EVENTS.sql,
      createParams(
        "offenderNos",
        offenderNumbers,
        "date",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date)),
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.toPrisonerSchedules()
  }

  fun getExternalTransfers(
    agencyId: String?,
    offenderNumbers: List<String>?,
    date: LocalDate?,
  ): List<PrisonerSchedule> {
    val schedules = jdbcTemplate.query(
      ScheduleRepositorySql.GET_EXTERNAL_TRANSFERS.sql + ScheduleRepositorySql.AND_OFFENDER_NUMBERS.sql,
      createParams(
        "offenderNos",
        offenderNumbers,
        "agencyId",
        agencyId,
        "date",
        SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date)),
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.toPrisonerSchedules()
  }

  fun getScheduledTransfersForPrisoner(
    prisonerNumber: String,
  ): List<PrisonerSchedule> {
    val schedules = jdbcTemplate.query(
      ScheduleRepositorySql.GET_SCHEDULED_TRANSFERS_FOR_PRISONER.sql,
      createParams(
        "prisonerNumber",
        prisonerNumber,
      ),
      EVENT_ROW_MAPPER,
    )
    return schedules.toPrisonerSchedules()
  }

  private companion object {
    private val EVENT_ROW_MAPPER = DataClassByColumnRowMapper(PrisonerScheduleDto::class.java)
  }
}

private fun List<PrisonerScheduleDto>.toPrisonerSchedules(): List<PrisonerSchedule> = map { it.toPrisonerSchedule() }
