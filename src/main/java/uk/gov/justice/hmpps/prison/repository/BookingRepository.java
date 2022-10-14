package uk.gov.justice.hmpps.prison.repository;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import uk.gov.justice.hmpps.prison.api.model.NewAppointment;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalculation;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetailDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummaryDto;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeDetail;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeDetailDto;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEventDto;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitBalancesDto;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.VisitDetailsDto;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.BookingRepositorySql;
import uk.gov.justice.hmpps.prison.repository.sql.InmateRepositorySql;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;
import uk.gov.justice.hmpps.prison.util.IQueryBuilder;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bookings API repository implementation.
 */
@Repository
@Slf4j
public class BookingRepository extends RepositoryBase {
    private static final RowMapper<PrivilegeDetailDto> PRIV_DETAIL_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(PrivilegeDetailDto.class);

    private static final DataClassByColumnRowMapper<ScheduledEventDto> EVENT_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(ScheduledEventDto.class);

    private final RowMapper<AlertResult> ALERTS_MAPPER = new StandardBeanPropertyRowMapper<>(AlertResult.class);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AlertResult {
        private Long bookingId;
        private String alertCode;
    }

    private final Map<String, FieldMapper> PAYABLE_ATTENDANCE_OUTCOMES_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
            .put("PAYABLE_ATTENDANCE_OUTCOMES_ID", new FieldMapper("payableAttendanceOutcomeId"))
            .put("EVENT_TYPE", new FieldMapper("eventType"))
            .put("OUTCOME_CODE", new FieldMapper("outcomeCode"))
            .put("PAY_FLAG", new FieldMapper("paid", value -> "Y".equalsIgnoreCase(value.toString())))
            .put("AUTHORISED_ABSENCE_FLAG", new FieldMapper("authorisedAbsence", value -> "Y".equalsIgnoreCase(value.toString())))
            .build();

    private static final RowMapper<OffenderSentenceCalculation> SENTENCE_CALC_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderSentenceCalculation.class);

    private static final RowMapper<VisitDetailsDto> VISIT_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(VisitDetailsDto.class);

    private final RowMapper<VisitBalancesDto> VISIT_BALANCES_MAPPER =
            new DataClassByColumnRowMapper<>(VisitBalancesDto.class);

    private static final RowMapper<OffenderSummaryDto> OFFENDER_SUMMARY_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(OffenderSummaryDto.class);

    private static final Map<String, FieldMapper> SENTENCE_DETAIL_MAPPING =
            new ImmutableMap.Builder<String, FieldMapper>()
                    .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
                    .put("SENTENCE_START_DATE", new FieldMapper("sentenceStartDate", DateTimeConverter::toISO8601LocalDate))
                    .put("SENTENCE_EXPIRY_DATE", new FieldMapper("sentenceExpiryDate", DateTimeConverter::toISO8601LocalDate))
                    .put("SED_OVERRIDED_DATE", new FieldMapper("sentenceExpiryOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("SED_CALCULATED_DATE", new FieldMapper("sentenceExpiryCalculatedDate", DateTimeConverter::toISO8601LocalDate))
                    .put("CRD_OVERRIDED_DATE", new FieldMapper("conditionalReleaseOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("CRD_CALCULATED_DATE", new FieldMapper("conditionalReleaseDate", DateTimeConverter::toISO8601LocalDate))
                    .put("ARD_OVERRIDED_DATE", new FieldMapper("automaticReleaseOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("ARD_CALCULATED_DATE", new FieldMapper("automaticReleaseDate", DateTimeConverter::toISO8601LocalDate))
                    .put("NPD_OVERRIDED_DATE", new FieldMapper("nonParoleOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("NPD_CALCULATED_DATE", new FieldMapper("nonParoleDate", DateTimeConverter::toISO8601LocalDate))
                    .put("PRRD_OVERRIDED_DATE", new FieldMapper("postRecallReleaseOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("PRRD_CALCULATED_DATE", new FieldMapper("postRecallReleaseDate", DateTimeConverter::toISO8601LocalDate))
                    .put("TARIFF_DATE", new FieldMapper("tariffDate", DateTimeConverter::toISO8601LocalDate))
                    .put("LICENCE_EXPIRY_DATE", new FieldMapper("licenceExpiryDate", DateTimeConverter::toISO8601LocalDate))
                    .put("LED_OVERRIDED_DATE", new FieldMapper("licenceExpiryOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("LED_CALCULATED_DATE", new FieldMapper("licenceExpiryCalculatedDate", DateTimeConverter::toISO8601LocalDate))
                    .put("PAROLE_ELIGIBILITY_DATE", new FieldMapper("paroleEligibilityDate", DateTimeConverter::toISO8601LocalDate))
                    .put("PED_OVERRIDED_DATE", new FieldMapper("paroleEligibilityOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("PED_CALCULATED_DATE", new FieldMapper("paroleEligibilityCalculatedDate", DateTimeConverter::toISO8601LocalDate))
                    .put("TUSED_OVERRIDED_DATE", new FieldMapper("topupSupervisionExpiryOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("TUSED_CALCULATED_DATE", new FieldMapper("topupSupervisionExpiryCalculatedDate", DateTimeConverter::toISO8601LocalDate))
                    .put("HDCED_OVERRIDED_DATE", new FieldMapper("homeDetentionCurfewEligibilityOverrideDate", DateTimeConverter::toISO8601LocalDate))
                    .put("HDCED_CALCULATED_DATE", new FieldMapper("homeDetentionCurfewEligibilityCalculatedDate", DateTimeConverter::toISO8601LocalDate))
                    .put("HOME_DET_CURF_ELIGIBILITY_DATE", new FieldMapper("homeDetentionCurfewEligibilityDate", DateTimeConverter::toISO8601LocalDate))
                    .put("HOME_DET_CURF_ACTUAL_DATE", new FieldMapper("homeDetentionCurfewActualDate", DateTimeConverter::toISO8601LocalDate))
                    .put("ACTUAL_PAROLE_DATE", new FieldMapper("actualParoleDate", DateTimeConverter::toISO8601LocalDate))
                    .put("RELEASE_ON_TEMP_LICENCE_DATE", new FieldMapper("releaseOnTemporaryLicenceDate", DateTimeConverter::toISO8601LocalDate))
                    .put("EARLY_REMOVAL_SCHEME_ELIG_DATE", new FieldMapper("earlyRemovalSchemeEligibilityDate", DateTimeConverter::toISO8601LocalDate))
                    .put("TOPUP_SUPERVISION_EXPIRY_DATE", new FieldMapper("topupSupervisionExpiryDate", DateTimeConverter::toISO8601LocalDate))
                    .put("EARLY_TERM_DATE", new FieldMapper("earlyTermDate", DateTimeConverter::toISO8601LocalDate))
                    .put("MID_TERM_DATE", new FieldMapper("midTermDate", DateTimeConverter::toISO8601LocalDate))
                    .put("LATE_TERM_DATE", new FieldMapper("lateTermDate", DateTimeConverter::toISO8601LocalDate))
                    .put("DPRRD_CALCULATED_DATE", new FieldMapper("dtoPostRecallReleaseDate", DateTimeConverter::toISO8601LocalDate))
                    .put("DPRRD_OVERRIDED_DATE", new FieldMapper("dtoPostRecallReleaseDateOverride", DateTimeConverter::toISO8601LocalDate))
                    .put("TARIFF_ERS_SCHEME_ELIG_DATE", new FieldMapper("tariffEarlyRemovalSchemeEligibilityDate", DateTimeConverter::toISO8601LocalDate))
                    .put("EFFECTIVE_SENTENCE_END_DATE", new FieldMapper("effectiveSentenceEndDate", DateTimeConverter::toISO8601LocalDate))
                    .put("ADDITIONAL_DAYS_AWARDED", new FieldMapper("additionalDaysAwarded"))
                    .build();

    private static final Map<String, FieldMapper> SENTENCE_DETAIL_ROW_MAPPER;

    static {
        final Map<String, FieldMapper> builderMap = new HashMap<>();
        builderMap.put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"));
        builderMap.put("OFFENDER_NO", new FieldMapper("offenderNo"));
        builderMap.put("FIRST_NAME", new FieldMapper("firstName"));
        builderMap.put("LAST_NAME", new FieldMapper("lastName"));
        builderMap.put("DATE_OF_BIRTH", new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate));
        builderMap.put("AGENCY_LOCATION_ID", new FieldMapper("agencyLocationId"));
        builderMap.put("AGENCY_LOCATION_DESC", new FieldMapper("agencyLocationDesc"));
        builderMap.put("MOST_RECENT_ACTIVE_BOOKING", new FieldMapper("mostRecentActiveBooking"));
        builderMap.put("INTERNAL_LOCATION_DESC", new FieldMapper("internalLocationDesc"));
        builderMap.put("FACIAL_IMAGE_ID", new FieldMapper("facialImageId"));
        builderMap.put("CONFIRMED_RELEASE_DATE", new FieldMapper("confirmedReleaseDate", DateTimeConverter::toISO8601LocalDate));
        SENTENCE_DETAIL_MAPPING.forEach(builderMap::putIfAbsent);
        SENTENCE_DETAIL_ROW_MAPPER = Collections.unmodifiableMap(builderMap);
    }

    public boolean verifyBookingAccess(final Long bookingId, final Set<String> agencyIds) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        Objects.requireNonNull(agencyIds, "agencyIds is a required parameter");

        final var initialSql = BookingRepositorySql.CHECK_BOOKING_AGENCIES.getSql();

        Long response;

        try {
            log.debug("Verifying access for booking [{}] in caseloads {}", bookingId, agencyIds);

            response = jdbcTemplate.queryForObject(
                    initialSql,
                    createParams("bookingId", bookingId, "agencyIds", agencyIds),
                    Long.class);
        } catch (final EmptyResultDataAccessException ex) {
            response = null;
        }

        return bookingId.equals(response);
    }

    public boolean checkBookingExists(final Long bookingId) {
        log.debug("Verifying booking [{}] exists", bookingId);

        return getBookingAgency(bookingId).isPresent();
    }

    public Optional<String> getBookingAgency(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = BookingRepositorySql.GET_OFFENDER_BOOKING_AGENCY.getSql();
        String agencyId;
        try {
            agencyId = jdbcTemplate.queryForObject(initialSql, createParams("bookingId", bookingId), String.class);
        } catch (final EmptyResultDataAccessException ex) {
            agencyId = null;
        }
        return Optional.ofNullable(agencyId);
    }

    public Optional<SentenceCalcDates> getBookingSentenceCalcDates(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_SENTENCE_DETAIL.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, SENTENCE_DETAIL_MAPPING);
        final var sql = builder.build();

        final var sentenceDetailRowMapper =
            Row2BeanRowMapper.makeMapping(SentenceCalcDates.class, SENTENCE_DETAIL_MAPPING);

        SentenceCalcDates sentenceDetail;

        try {
            sentenceDetail =
                    jdbcTemplate.queryForObject(
                            sql,
                            createParams("bookingId", bookingId),
                            sentenceDetailRowMapper);

        } catch (final EmptyResultDataAccessException ex) {
            sentenceDetail = null;
        }

        return Optional.ofNullable(sentenceDetail);
    }

    public Map<Long, List<PrivilegeDetail>> getBookingIEPDetailsByBookingIds(final List<Long> bookingIds) {
        Objects.requireNonNull(bookingIds, "bookingIds are a required parameter");
        final var privs = jdbcTemplate.query(
                BookingRepositorySql.GET_BOOKING_IEP_DETAILS_BY_IDS.getSql(),
                createParams("bookingIds", bookingIds),
                PRIV_DETAIL_ROW_MAPPER);

        return privs.stream()
            .map(PrivilegeDetailDto::toPrivilegeDetail)
            .collect(Collectors.groupingBy(PrivilegeDetail::getBookingId));
    }

    /**
     * @param bookingIds Booking IDs
     * @param cutoffDate Omit alerts which have expired before this date-time
     * @return a list of active alert codes for each booking id
     */
    public Map<Long, List<String>> getAlertCodesForBookings(final List<Long> bookingIds, final LocalDateTime cutoffDate) {
        if (CollectionUtils.isEmpty(bookingIds)) {
            return Collections.emptyMap();
        }
        final var results = jdbcTemplate.query(
                InmateRepositorySql.GET_ALERT_CODES_FOR_BOOKINGS.getSql(),
                createParams("bookingIds", bookingIds, "cutoffDate", DateTimeConverter.fromLocalDateTime(cutoffDate)),
                ALERTS_MAPPER);

        return results.stream().collect(Collectors.groupingBy(AlertResult::getBookingId,
                Collectors.mapping(AlertResult::getAlertCode, Collectors.toList())));
    }

    public Page<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_ACTIVITIES.getSql() + BookingRepositorySql.ACTIVITIES_BOOKING_ID_CLAUSE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = buildOrderAndPagination(orderByFields, order, builder);

        final var paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        final var activityDtos = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);
        final var activities = activityDtos.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());

        return new Page<>(activities, paRowMapper.getTotalRecords(), offset, limit);
    }

    public List<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_ACTIVITIES.getSql() + BookingRepositorySql.ACTIVITIES_BOOKING_ID_CLAUSE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        final var activities = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
        return activities.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());
    }

    public List<ScheduledEvent> getBookingActivities(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_ACTIVITIES.getSql() + BookingRepositorySql.ACTIVITIES_BOOKING_ID_IN_CLAUSE.getSql();

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        final var activities = jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
        return activities.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());
    }

    public void updateAttendance(final Long bookingId, final Long activityId, final UpdateAttendance updateAttendance, final boolean paid, final boolean authorisedAbsence) {
        final var sql = BookingRepositorySql.UPDATE_ATTENDANCE.getSql();
        final var rows = jdbcTemplate.update(
                sql,
                createParams(
                        "bookingId", bookingId,
                        "eventId", activityId,
                        "eventOutcome", updateAttendance.getEventOutcome(),
                        "performanceCode", updateAttendance.getPerformance(),
                        "commentText", updateAttendance.getOutcomeComment(),
                        "paid", paid ? "Y" : "N",
                        "authorisedAbsence", authorisedAbsence ? "Y" : "N"
                ));
        if (rows != 1) {
            throw EntityNotFoundException.withMessage("Activity with booking Id %d and activityId %d not found",
                    bookingId, activityId);
        }
    }

    public LocalDate getAttendanceEventDate(final Long activityId) {
        final Date result;
        try {
            result = jdbcTemplate.queryForObject(
                    BookingRepositorySql.GET_ATTENDANCE_DATE.getSql(),
                    createParams("eventId", activityId),
                    Date.class);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
        return DateTimeConverter.toISO8601LocalDate(result);
    }

    public PayableAttendanceOutcomeDto getPayableAttendanceOutcome(final String eventType, final String outcomeCode) {

        Objects.requireNonNull(eventType, "eventType is a required parameter");
        Objects.requireNonNull(outcomeCode, "outcomeCode is a required parameter");

        final var builder = queryBuilderFactory.getQueryBuilder(
                BookingRepositorySql.GET_PAYABLE_ATTENDANCE_OUTCOMES.getSql(),
                PAYABLE_ATTENDANCE_OUTCOMES_MAPPING);
        final var sql = builder.build();

        final var inmateRowMapper = Row2BeanRowMapper.makeMapping(
            PayableAttendanceOutcomeDto.class, PAYABLE_ATTENDANCE_OUTCOMES_MAPPING);

        return jdbcTemplate.queryForObject(
                sql,
                createParams("eventType", eventType, "outcomeCode", outcomeCode),
                inmateRowMapper);
    }

    public Page<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_VISITS.getSql() + BookingRepositorySql.VISITS_BOOKING_ID_CLAUSE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = buildOrderAndPagination(orderByFields, order, builder);

        final var paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        final var visitDtos = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);
        final var visits = visitDtos.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());

        return new Page<>(visits, paRowMapper.getTotalRecords(), offset, limit);
    }

    private String buildOrderAndPagination(final String orderByFields, final Order order, final IQueryBuilder builder) {
        return builder
                .addRowCount()
                .addOrderBy(order, orderByFields)
                .addPagination()
                .build();
    }

    public List<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_VISITS.getSql() + BookingRepositorySql.VISITS_BOOKING_ID_CLAUSE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        final var events = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
        return events.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());
    }

    public Optional<VisitBalances> getBookingVisitBalances(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingIds is a required parameter");
        final var sql = BookingRepositorySql.FIND_VO_PVO_BALANCE_DETAILS.getSql();

        VisitBalancesDto visitBalances;
        try {
            visitBalances = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    VISIT_BALANCES_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            visitBalances = null;
        }
        return Optional.ofNullable(visitBalances).map(VisitBalancesDto::toVisitBalances);
    }

    public boolean createBookingVisitOrderBalances(final Long bookingId, final Integer voBalance, final Integer pvoBalance) {
        Objects.requireNonNull(bookingId, "bookingIds is a required parameter");
        final var sql = BookingRepositorySql.INSERT_VO_PVO_BALANCE.getSql();

        VisitBalancesDto visitBalances;

        return jdbcTemplate.update(
                sql,
                createParams("bookingId", bookingId, "voBalance", voBalance, "pvoBalance", pvoBalance)) == 1;
    }

    public List<ScheduledEvent> getBookingVisits(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_VISITS.getSql() + BookingRepositorySql.VISITS_BOOKING_ID_IN_CLAUSE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        final var events = jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
        return events.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());
    }

    public Optional<VisitDetails> getBookingVisitNext(final Long bookingId, final LocalDateTime from) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        Objects.requireNonNull(from, "from is a required parameter");

        try {
            final var result = jdbcTemplate.queryForObject(
                    BookingRepositorySql.GET_NEXT_BOOKING_VISIT.getSql(),
                    createParams("bookingId", bookingId, "fromDate", DateTimeConverter.fromLocalDateTime(from)),
                    VISIT_ROW_MAPPER);
            return Optional.of(result)
                .map(vd -> vd.toVisitDetails().toBuilder().leadVisitor(StringUtils.trimToNull(vd.getLeadVisitor())).build());
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<OffenderBookingIdSeq> getLatestBookingIdentifierForOffender(final String offenderNo) {
        Validate.notBlank("Offender number must be specified.");
        final var sql = BookingRepositorySql.FIND_BOOKING_IDS_BY_OFFENDER_NO.getSql();

        return jdbcTemplate.query(sql, createParams("offenderNo", offenderNo), new StandardBeanPropertyRowMapper<>(OffenderBookingSeq.class))
                .stream()
                .findFirst()
                .map(r -> new OffenderBookingIdSeq(r.offenderNo, r.bookingId, r.bookingSeq));
    }

    @Data
    private static class OffenderBookingSeq {
        private String offenderNo;
        private Long bookingId;
        private Integer bookingSeq;
    }

    public List<OffenderSummary> getBookingsByRelationship(final String externalRef, final String relationshipType, final String identifierType) {

        final var sql = BookingRepositorySql.FIND_BOOKINGS_BY_PERSON_CONTACT.getSql();

        final var summaries = jdbcTemplate.query(
                sql,
                createParams("identifierType", identifierType,
                        "identifier", externalRef,
                        "relationshipType", relationshipType),
                OFFENDER_SUMMARY_ROW_MAPPER);
        return summaries.stream().map(OffenderSummaryDto::toOffenderSummary).toList();
    }

    public List<OffenderSummary> getBookingsByRelationship(final Long personId, final String relationshipType) {

        final var sql = BookingRepositorySql.FIND_BOOKINGS_BY_PERSON_ID_CONTACT.getSql();

        final var summaries = jdbcTemplate.query(
                sql,
                createParams(
                        "personId", personId,
                        "relationshipType", relationshipType),
                OFFENDER_SUMMARY_ROW_MAPPER);
        return summaries.stream().map(OffenderSummaryDto::toOffenderSummary).toList();
    }

    public Page<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_APPOINTMENTS.getSql() + BookingRepositorySql.APPOINTMENTS_BOOKING_ID_CLAUSE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = buildOrderAndPagination(orderByFields, order, builder);

        final var paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        final var visitDtos = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);
        final var visits = visitDtos.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());

        return new Page<>(visits, paRowMapper.getTotalRecords(), offset, limit);
    }

    private MapSqlParameterSource buildParams(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit) {
        return createParams("bookingId", bookingId,
                "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
                "offset", offset,
                "limit", limit);
    }

    public List<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_APPOINTMENTS.getSql() + BookingRepositorySql.APPOINTMENTS_BOOKING_ID_CLAUSE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        final var events = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
        return events.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());
    }

    public List<ScheduledEvent> getBookingAppointments(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        final var initialSql = BookingRepositorySql.GET_BOOKING_APPOINTMENTS.getSql() + BookingRepositorySql.APPOINTMENTS_BOOKING_ID_IN_CLAUSE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        final var events = jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
        return events.stream().map(ScheduledEventDto::toScheduledEvent).collect(Collectors.toList());
    }

    public Optional<ScheduledEvent> getBookingAppointmentByEventId(final long eventId) {
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                    BookingRepositorySql.GET_BOOKING_APPOINTMENT_BY_EVENT_ID.getSql(),
                    createParams("eventId", eventId),
                    EVENT_ROW_MAPPER)).map(ScheduledEventDto::toScheduledEvent);
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long createBookingAppointment(final Long bookingId, final NewAppointment newAppointment, final String agencyId) {
        final var sql = BookingRepositorySql.INSERT_APPOINTMENT.getSql();
        final var generatedKeyHolder = new GeneratedKeyHolder();
        final var startTime = newAppointment.getStartTime();
        jdbcTemplate.update(
                sql,
                createParams("bookingId", bookingId,
                        "eventSubType", newAppointment.getAppointmentType(),
                        "eventDate", DateTimeConverter.toDate(startTime.toLocalDate()),
                        "startTime", DateTimeConverter.fromLocalDateTime(startTime),
                        "endTime", DateTimeConverter.fromLocalDateTime(newAppointment.getEndTime()),
                        "comment", newAppointment.getComment(),
                        "locationId", newAppointment.getLocationId(),
                        "agencyId", agencyId),
                generatedKeyHolder,
                new String[]{"EVENT_ID"});
        return generatedKeyHolder.getKey().longValue();
    }

    public void deleteBookingAppointment(final long eventId) {
        // Not deleting a row because it doesn't exist isn't an error.
        jdbcTemplate.update(
                BookingRepositorySql.DELETE_APPOINTMENT.getSql(),
                createParams("eventId", eventId)
        );
    }

    /**
     * Update the comment field of an OFFENDER_IND_SCHEDULES record if it's event class is 'INT_MOV' and
     * its event type is 'APP'
     * @param eventId The unique identifier for the appointment
     * @param comment The value to which the appointment should be set. Can be null.
     * @return true if the appointment was updated. false if the appointment does not exist.
     */
    public boolean updateBookingAppointmentComment(final long eventId, final String comment) {
        return jdbcTemplate.update(
            BookingRepositorySql.UPDATE_APPOINTMENT_COMMENT.getSql(),
            createParams(
                "eventId", eventId,
                "comment", comment
            )
        ) == 1;
    }

    public List<OffenderSentenceDetailDto> getOffenderSentenceSummary(final String query, final Set<String> allowedCaseloadsOnly, final boolean filterByCaseload, final boolean viewInactiveBookings) {
        var initialSql = BookingRepositorySql.GET_OFFENDER_SENTENCE_DETAIL.getSql();

        final var additionSql = new StringBuilder();
        if (!viewInactiveBookings) {
            appendWhereOrAnd(additionSql);
            additionSql.append("OB.ACTIVE_FLAG = :activeFlag AND OB.BOOKING_SEQ = :bookingSeq");

        }
        if (filterByCaseload) {
            // if we are filtering by caseload but the user doesn't have any then shouldn't get any results
            if (allowedCaseloadsOnly.isEmpty()) return Collections.emptyList();
            appendWhereOrAnd(additionSql);
            additionSql.append(InmateRepositorySql.CASELOAD_FILTER.getSql());
        }

        if (additionSql.length() > 0) {
            initialSql += additionSql.toString();
        }

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, SENTENCE_DETAIL_ROW_MAPPER);

        final var sql = builder.addQuery(query).build();

        final var offenderSentenceDetailDtoRowMapper = Row2BeanRowMapper.makeMapping(OffenderSentenceDetailDto.class, SENTENCE_DETAIL_ROW_MAPPER);

        return jdbcTemplate.query(
                sql,
                createParams("caseLoadId", allowedCaseloadsOnly, "activeFlag", "Y", "bookingSeq", 1),
                offenderSentenceDetailDtoRowMapper);
    }

    private void appendWhereOrAnd(final StringBuilder additionSql) {
        if (additionSql.length() == 0) {
            additionSql.append(" WHERE ");
        } else {
            additionSql.append(" AND ");
        }
    }

    public Optional<OffenderSummary> getLatestBookingByBookingId(final Long bookingId) {
        Validate.notNull(bookingId, "Booking id must be specified.");

        final var sql = BookingRepositorySql.GET_LATEST_BOOKING_BY_BOOKING_ID.getSql();

        OffenderSummaryDto summary;

        try {
            summary = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    OFFENDER_SUMMARY_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            summary = null;
        }

        return Optional.ofNullable(summary).map(OffenderSummaryDto::toOffenderSummary);
    }

    public Optional<OffenderSummary> getLatestBookingByOffenderNo(final String offenderNo) {
        Validate.notBlank("Offender number must be specified.");

        final var sql = BookingRepositorySql.GET_LATEST_BOOKING_BY_OFFENDER_NO.getSql();

        OffenderSummaryDto summary;

        try {
            summary = jdbcTemplate.queryForObject(
                    sql,
                    createParams("offenderNo", offenderNo),
                    OFFENDER_SUMMARY_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            summary = null;
        }

        return Optional.ofNullable(summary).map(OffenderSummaryDto::toOffenderSummary);
    }

    public List<OffenderSentenceCalculation> getOffenderSentenceCalculations(final Set<String> agencyIds) {
        final var sql = BookingRepositorySql.GET_OFFENDER_SENT_CALCULATIONS.getSql();
        return jdbcTemplate
                .query(
                        sql,
                        createParams("agencyIds", agencyIds, "activeFlag", "Y", "bookingSeq", 1),
                        SENTENCE_CALC_ROW_MAPPER);
    }

    public long createAppointment(final AppointmentDetails details, final AppointmentDefaults defaults, final String agencyId) {
        final var generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
            BookingRepositorySql.INSERT_APPOINTMENT.getSql(),
            toSqlParameterSource(details, defaults, agencyId),
            generatedKeyHolder,
            new String[]{"EVENT_ID"});
        return generatedKeyHolder.getKey().longValue();
    }

    private SqlParameterSource toSqlParameterSource(final AppointmentDetails details, final AppointmentDefaults defaults, final String agencyId) {
        return createParams(
                "bookingId", details.getBookingId(),
                "eventSubType", defaults.getAppointmentType(),
                "eventDate", DateTimeConverter.toDate(details.getStartTime().toLocalDate()),
                "startTime", DateTimeConverter.fromLocalDateTime(details.getStartTime()),
                "endTime", DateTimeConverter.fromLocalDateTime(details.getEndTime()),
                "locationId", defaults.getLocationId(),
                "agencyId", agencyId,
                "comment", details.getComment());
    }

    public List<Long> findBookingsIdsInAgency(final List<Long> bookingIds, final String agencyId) {
        if (bookingIds.isEmpty()) return Collections.emptyList();

        return jdbcTemplate.query(
                BookingRepositorySql.FIND_BOOKING_IDS_IN_AGENCY.getSql(),
                createParams("bookingIds", bookingIds, "agencyId", agencyId),
                (rs, rowNum) -> rs.getLong(1));
    }
}
