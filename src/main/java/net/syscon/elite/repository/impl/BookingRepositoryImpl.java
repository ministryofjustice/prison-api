package net.syscon.elite.repository.impl;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.syscon.elite.api.model.IepLevelAndComment;
import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.NewBooking;
import net.syscon.elite.api.model.OffenderSentenceCalculation;
import net.syscon.elite.api.model.OffenderSentenceDetailDto;
import net.syscon.elite.api.model.OffenderSentenceTerms;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.PrivilegeDetail;
import net.syscon.elite.api.model.RecallBooking;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.api.model.UpdateAttendance;
import net.syscon.elite.api.model.Visit;
import net.syscon.elite.api.model.VisitBalances;
import net.syscon.elite.api.model.bulkappointments.AppointmentDefaults;
import net.syscon.elite.api.model.bulkappointments.AppointmentDetails;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.PayableAttendanceOutcomeDto;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

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
public class BookingRepositoryImpl extends RepositoryBase implements BookingRepository {

    private static final String ACTIVITIES_BOOKING_ID_CLAUSE = " AND OPP.OFFENDER_BOOK_ID = :bookingId";
    private static final String ACTIVITIES_BOOKING_ID_IN_CLAUSE = " AND OPP.OFFENDER_BOOK_ID IN (:bookingIds)";
    private static final String VISITS_BOOKING_ID_CLAUSE = " AND VIS.OFFENDER_BOOK_ID = :bookingId";
    private static final String VISITS_BOOKING_ID_IN_CLAUSE = " AND VIS.OFFENDER_BOOK_ID IN (:bookingIds)";
    private static final String APPOINTMENTS_BOOKING_ID_CLAUSE = " AND OIS.OFFENDER_BOOK_ID = :bookingId";
    private static final String APPOINTMENTS_BOOKING_ID_IN_CLAUSE = " AND OIS.OFFENDER_BOOK_ID IN (:bookingIds)";

    private static final StandardBeanPropertyRowMapper<PrivilegeDetail> PRIV_DETAIL_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrivilegeDetail.class);

    private static final StandardBeanPropertyRowMapper<ScheduledEvent> EVENT_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(ScheduledEvent.class);

    private final StandardBeanPropertyRowMapper<AlertResult> ALERTS_MAPPER = new StandardBeanPropertyRowMapper<>(AlertResult.class);

    private final CreateBookingImpl createBookingRepository;
    private final RecallBookingImpl recallBookingRepository;

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

    private static final StandardBeanPropertyRowMapper<OffenderSentenceCalculation> SENTENCE_CALC_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderSentenceCalculation.class);

    private static final StandardBeanPropertyRowMapper<OffenderSentenceTerms> SENTENCE_TERMS_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderSentenceTerms.class);

    private static final StandardBeanPropertyRowMapper<Visit> VISIT_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Visit.class);

    private final StandardBeanPropertyRowMapper<VisitBalances> VISIT_BALANCES_MAPPER =
            new StandardBeanPropertyRowMapper<>(VisitBalances.class);


    private static final StandardBeanPropertyRowMapper<OffenderSummary> OFFENDER_SUMMARY_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderSummary.class);

    private static final Map<String, FieldMapper> SENTENCE_DETAIL_MAPPING =
            new ImmutableMap.Builder<String, FieldMapper>()
                    .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
                    .put("SENTENCE_START_DATE", new FieldMapper("sentenceStartDate", DateTimeConverter::toISO8601LocalDate))
                    .put("SENTENCE_EXPIRY_DATE", new FieldMapper("sentenceExpiryDate", DateTimeConverter::toISO8601LocalDate))
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
                    .put("PAROLE_ELIGIBILITY_DATE", new FieldMapper("paroleEligibilityDate", DateTimeConverter::toISO8601LocalDate))
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
                    .put("TARIFF_EARLY_REMOVAL_SCHEME_ELIG_DATE", new FieldMapper("tariffEarlyRemovalSchemeEligibilityDate", DateTimeConverter::toISO8601LocalDate))
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
        builderMap.put("INTERNAL_LOCATION_DESC", new FieldMapper("internalLocationDesc"));
        builderMap.put("FACIAL_IMAGE_ID", new FieldMapper("facialImageId"));
        builderMap.put("CONFIRMED_RELEASE_DATE", new FieldMapper("confirmedReleaseDate", DateTimeConverter::toISO8601LocalDate));
        SENTENCE_DETAIL_MAPPING.forEach(builderMap::putIfAbsent);
        SENTENCE_DETAIL_ROW_MAPPER = Collections.unmodifiableMap(builderMap);
    }

    public BookingRepositoryImpl(final CreateBookingImpl createBookingRepository, final RecallBookingImpl recallBookingRepository) {
        this.createBookingRepository = createBookingRepository;
        this.recallBookingRepository = recallBookingRepository;
    }

    @Override
    public boolean verifyBookingAccess(final Long bookingId, final Set<String> agencyIds) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        Objects.requireNonNull(agencyIds, "agencyIds is a required parameter");

        final var initialSql = getQuery("CHECK_BOOKING_AGENCIES");

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

    @Override
    public boolean checkBookingExists(final Long bookingId) {
        log.debug("Verifying booking [{}] exists", bookingId);

        return getBookingAgency(bookingId).isPresent();
    }

    @Override
    public Optional<String> getBookingAgency(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = getQuery("GET_OFFENDER_BOOKING_AGENCY");
        String agencyId;
        try {
            agencyId = jdbcTemplate.queryForObject(initialSql, createParams("bookingId", bookingId), String.class);
        } catch (final EmptyResultDataAccessException ex) {
            agencyId = null;
        }
        return Optional.ofNullable(agencyId);
    }

    @Override
    public Optional<SentenceDetail> getBookingSentenceDetail(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_SENTENCE_DETAIL");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, SENTENCE_DETAIL_MAPPING);
        final var sql = builder.build();

        final var sentenceDetailRowMapper =
                Row2BeanRowMapper.makeMapping(sql, SentenceDetail.class, SENTENCE_DETAIL_MAPPING);

        SentenceDetail sentenceDetail;

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

    @Override
    public Map<Long, List<PrivilegeDetail>> getBookingIEPDetailsByBookingIds(final List<Long> bookingIds) {
        Objects.requireNonNull(bookingIds, "bookingIds are a required parameter");
        final var privs = jdbcTemplate.query(
                getQuery("GET_BOOKING_IEP_DETAILS_BY_IDS"),
                createParams("bookingIds", bookingIds),
                PRIV_DETAIL_ROW_MAPPER);

        return privs.stream().collect(Collectors.groupingBy(PrivilegeDetail::getBookingId));
    }

    @Override
    public void addIepLevel(Long bookingId, String username, IepLevelAndComment iepLevel) {
        val now = LocalDateTime.now();

        jdbcTemplate.update(
                getQuery("ADD_IEP_LEVEL"),
                createParams(
                        "bookingId", bookingId,
                        "userId", username,
                        "date", DateTimeConverter.toDate(now.toLocalDate()),
                        "time", DateTimeConverter.toDate(now),
                        "iepLevel", iepLevel.getIepLevel(),
                        "comment", iepLevel.getComment())
        );
    }

    @Override
    public Set<String> getIepLevelsForAgencySelectedByBooking(long bookingId) {
        final List<String> iepLevels = jdbcTemplate.queryForList(
                getQuery("IEP_LEVELS_FOR_AGENCY_SELECTED_BY_BOOKING"),
                Map.of("bookingId", bookingId),
                String.class
        );
        return java.util.Set.copyOf(iepLevels);
    }

    /**
     * @param bookingIds
     * @param cutoffDate Omit alerts which have expired before this date-time
     * @return a list of active alert codes for each booking id
     */
    @Override
    public Map<Long, List<String>> getAlertCodesForBookings(final List<Long> bookingIds, final LocalDateTime cutoffDate) {
        if (CollectionUtils.isEmpty(bookingIds)) {
            return Collections.emptyMap();
        }
        final var results = jdbcTemplate.query(
                getQuery("GET_ALERT_CODES_FOR_BOOKINGS"),
                createParams("bookingIds", bookingIds, "cutoffDate", DateTimeConverter.fromLocalDateTime(cutoffDate)),
                ALERTS_MAPPER);

        return results.stream().collect(Collectors.groupingBy(AlertResult::getBookingId,
                Collectors.mapping(AlertResult::getAlertCode, Collectors.toList())));
    }

    @Override
    public Page<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_ACTIVITIES") + ACTIVITIES_BOOKING_ID_CLAUSE;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = buildOrderAndPagination(orderByFields, order, builder);

        final var paRowMapper = new PageAwareRowMapper<ScheduledEvent>(EVENT_ROW_MAPPER);

        final var activities = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);

        return new Page<>(activities, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public List<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_ACTIVITIES") + ACTIVITIES_BOOKING_ID_CLAUSE;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<ScheduledEvent> getBookingActivities(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_ACTIVITIES") + ACTIVITIES_BOOKING_ID_IN_CLAUSE;

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public void updateAttendance(final Long bookingId, final Long activityId, final UpdateAttendance updateAttendance, final boolean paid, final boolean authorisedAbsence) {
        final var sql = getQuery("UPDATE_ATTENDANCE");
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

    @Override
    public LocalDate getAttendanceEventDate(final Long activityId) {
        final Date result;
        try {
            result = jdbcTemplate.queryForObject(
                    getQuery("GET_ATTENDANCE_DATE"),
                    createParams("eventId", activityId),
                    Date.class);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
        return DateTimeConverter.toISO8601LocalDate(result);
    }

    @Override
    public PayableAttendanceOutcomeDto getPayableAttendanceOutcome(final String eventType, final String outcomeCode) {

        Objects.requireNonNull(eventType, "eventType is a required parameter");
        Objects.requireNonNull(outcomeCode, "outcomeCode is a required parameter");

        final var builder = queryBuilderFactory.getQueryBuilder(
                getQuery("GET_PAYABLE_ATTENDANCE_OUTCOMES"),
                PAYABLE_ATTENDANCE_OUTCOMES_MAPPING);
        final var sql = builder.build();

        final var inmateRowMapper = Row2BeanRowMapper.makeMapping(
                sql, PayableAttendanceOutcomeDto.class, PAYABLE_ATTENDANCE_OUTCOMES_MAPPING);

        return jdbcTemplate.queryForObject(
                sql,
                createParams("eventType", eventType, "outcomeCode", outcomeCode),
                inmateRowMapper);
    }

    @Override
    public Page<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_VISITS") + VISITS_BOOKING_ID_CLAUSE;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = buildOrderAndPagination(orderByFields, order, builder);

        final var paRowMapper = new PageAwareRowMapper<ScheduledEvent>(EVENT_ROW_MAPPER);

        final var visits = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);

        return new Page<>(visits, paRowMapper.getTotalRecords(), offset, limit);
    }

    private String buildOrderAndPagination(final String orderByFields, final Order order, final IQueryBuilder builder) {
        return builder
                .addRowCount()
                .addOrderBy(order, orderByFields)
                .addPagination()
                .build();
    }

    @Override
    public List<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_VISITS") + VISITS_BOOKING_ID_CLAUSE;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public Optional<VisitBalances> getBookingVisitBalances(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingIds is a required parameter");
        final var sql = getQuery("FIND_REMAINING_VO_PVO");

        VisitBalances visitBalances;
        try {
            visitBalances = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    VISIT_BALANCES_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            visitBalances = null;
        }
        return Optional.ofNullable(visitBalances);
    }

    @Override
    public List<ScheduledEvent> getBookingVisits(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_VISITS") + VISITS_BOOKING_ID_IN_CLAUSE;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public Visit getBookingVisitLast(final Long bookingId, final LocalDateTime cutoffDate) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        Objects.requireNonNull(cutoffDate, "cutoffDate is a required parameter");

        try {
            final var result = jdbcTemplate.queryForObject(//
                    getQuery("GET_LAST_BOOKING_VISIT"),
                    createParams("bookingId", bookingId, "cutoffDate", DateTimeConverter.fromLocalDateTime(cutoffDate)),
                    VISIT_ROW_MAPPER);
            result.setLeadVisitor(StringUtils.trimToNull(result.getLeadVisitor()));
            return result;
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Visit getBookingVisitNext(final Long bookingId, final LocalDateTime from) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        Objects.requireNonNull(from, "from is a required parameter");

        try {
            final var result = jdbcTemplate.queryForObject(
                    getQuery("GET_NEXT_BOOKING_VISIT"),
                    createParams("bookingId", bookingId, "fromDate", DateTimeConverter.fromLocalDateTime(from)),
                    VISIT_ROW_MAPPER);
            result.setLeadVisitor(StringUtils.trimToNull(result.getLeadVisitor()));
            return result;
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Optional<Long> getBookingIdByOffenderNo(final String offenderNo) {
        Validate.notBlank("Offender number must be specified.");

        final var sql = getQuery("FIND_BOOKING_ID_BY_OFFENDER_NO");

        Long bookingId;

        try {
            bookingId = jdbcTemplate.queryForObject(
                    sql,
                    createParams("offenderNo", offenderNo, "bookingSeq", 1), Long.class);
        } catch (final EmptyResultDataAccessException ex) {
            bookingId = null;
        }

        return Optional.ofNullable(bookingId);
    }

    @Override
    public List<OffenderSummary> getBookingsByRelationship(final String externalRef, final String relationshipType, final String identifierType) {

        final var sql = getQuery("FIND_BOOKINGS_BY_PERSON_CONTACT");

        return jdbcTemplate.query(
                sql,
                createParams("identifierType", identifierType,
                        "identifier", externalRef,
                        "relationshipType", relationshipType),
                OFFENDER_SUMMARY_ROW_MAPPER);
    }

    @Override
    public List<OffenderSummary> getBookingsByRelationship(final Long personId, final String relationshipType) {

        final var sql = getQuery("FIND_BOOKINGS_BY_PERSON_ID_CONTACT");

        return jdbcTemplate.query(
                sql,
                createParams(
                        "personId", personId,
                        "relationshipType", relationshipType),
                OFFENDER_SUMMARY_ROW_MAPPER);
    }

    @Override
    public Page<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_APPOINTMENTS") + APPOINTMENTS_BOOKING_ID_CLAUSE;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = buildOrderAndPagination(orderByFields, order, builder);

        final var paRowMapper = new PageAwareRowMapper<ScheduledEvent>(EVENT_ROW_MAPPER);

        final var visits = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);

        return new Page<>(visits, paRowMapper.getTotalRecords(), offset, limit);
    }

    private MapSqlParameterSource buildParams(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit) {
        return createParams("bookingId", bookingId,
                "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
                "offset", offset,
                "limit", limit);
    }

    @Override
    public List<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_APPOINTMENTS") + APPOINTMENTS_BOOKING_ID_CLAUSE;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<ScheduledEvent> getBookingAppointments(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        final var initialSql = getQuery("GET_BOOKING_APPOINTMENTS") + APPOINTMENTS_BOOKING_ID_IN_CLAUSE;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public ScheduledEvent getBookingAppointment(final Long bookingId, final Long eventId) {
        final var sql = getQuery("GET_BOOKING_APPOINTMENT");
        return jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "eventId", eventId),
                EVENT_ROW_MAPPER);
    }

    @Override
    public Long createBookingAppointment(final Long bookingId, final NewAppointment newAppointment, final String agencyId) {
        final var sql = getQuery("INSERT_APPOINTMENT");
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

    @Override
    public List<OffenderSentenceDetailDto> getOffenderSentenceSummary(final String query, final Set<String> allowedCaseloadsOnly, final boolean filterByCaseload, final boolean viewInactiveBookings) {
        var initialSql = getQuery("GET_OFFENDER_SENTENCE_DETAIL");

        final var additionSql = new StringBuilder();
        if (!viewInactiveBookings) {
            appendWhereOrAnd(additionSql);
            additionSql.append("OB.ACTIVE_FLAG = :activeFlag AND OB.BOOKING_SEQ = :bookingSeq");

        }
        if (filterByCaseload && !allowedCaseloadsOnly.isEmpty()) {
            appendWhereOrAnd(additionSql);
            additionSql.append(getQuery("CASELOAD_FILTER"));
        }

        if (additionSql.length() > 0) {
            initialSql += additionSql.toString();
        }

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, SENTENCE_DETAIL_ROW_MAPPER);

        final var sql = builder.addQuery(query).build();

        final var offenderSentenceDetailDtoRowMapper = Row2BeanRowMapper.makeMapping(sql, OffenderSentenceDetailDto.class, SENTENCE_DETAIL_ROW_MAPPER);

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

    @Override
    public Optional<OffenderSummary> getLatestBookingByBookingId(final Long bookingId) {
        Validate.notNull(bookingId, "Booking id must be specified.");

        final var sql = getQuery("GET_LATEST_BOOKING_BY_BOOKING_ID");

        OffenderSummary summary;

        try {
            summary = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    OFFENDER_SUMMARY_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            summary = null;
        }

        return Optional.ofNullable(summary);
    }

    @Override
    public Optional<OffenderSummary> getLatestBookingByOffenderNo(final String offenderNo) {
        Validate.notBlank("Offender number must be specified.");

        final var sql = getQuery("GET_LATEST_BOOKING_BY_OFFENDER_NO");

        OffenderSummary summary;

        try {
            summary = jdbcTemplate.queryForObject(
                    sql,
                    createParams("offenderNo", offenderNo),
                    OFFENDER_SUMMARY_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            summary = null;
        }

        return Optional.ofNullable(summary);
    }

    @Override
    public Long createBooking(final String agencyId, final NewBooking newBooking) {
        Validate.notNull(newBooking);

        return createBookingRepository.createBooking(agencyId, newBooking);
    }

    @Override
    public Long recallBooking(final String agencyId, final RecallBooking recallBooking) {
        Validate.notNull(recallBooking);

        return recallBookingRepository.recallBooking(agencyId, recallBooking);
    }

    @Override
    public List<OffenderSentenceCalculation> getOffenderSentenceCalculations(final Set<String> agencyIds) {
        final var sql = getQuery("GET_OFFENDER_SENT_CALCULATIONS");
        return jdbcTemplate
                .query(
                        sql,
                        createParams("agencyIds", agencyIds, "activeFlag", "Y", "bookingSeq", 1),
                        SENTENCE_CALC_ROW_MAPPER);
    }

    @Override
    public List<OffenderSentenceTerms> getOffenderSentenceTerms(final Long bookingId, final String sentenceTermCode) {
        final var sql = getQuery("GET_OFFENDER_SENTENCE_TERMS");
        return jdbcTemplate
                .query(
                        sql,
                        createParams("bookingId", bookingId, "sentenceTermCode", sentenceTermCode),
                        SENTENCE_TERMS_ROW_MAPPER);
    }

    @Override
    public void createMultipleAppointments(final List<AppointmentDetails> flattenedDetails, final AppointmentDefaults defaults, final String agencyId) {
        jdbcTemplate.batchUpdate(
                getQuery("INSERT_APPOINTMENT"),
                toSqlParameterSourceList(flattenedDetails, defaults, agencyId));
    }

    private SqlParameterSource[] toSqlParameterSourceList(final List<AppointmentDetails> flattenedDetails, final AppointmentDefaults defaults, final String agencyId) {
        return flattenedDetails
                .stream()
                .map(appointment -> createParams(
                        "bookingId", appointment.getBookingId(),
                        "eventSubType", defaults.getAppointmentType(),
                        "eventDate", DateTimeConverter.toDate(appointment.getStartTime().toLocalDate()),
                        "startTime", DateTimeConverter.fromLocalDateTime(appointment.getStartTime()),
                        "endTime", DateTimeConverter.fromLocalDateTime(appointment.getEndTime()),
                        "locationId", defaults.getLocationId(),
                        "agencyId", agencyId,
                        "comment", appointment.getComment()
                )).toArray(SqlParameterSource[]::new);
    }

    @Override
    public List<Long> findBookingsIdsInAgency(final List<Long> bookingIds, final String agencyId) {
        if (bookingIds.isEmpty()) return Collections.emptyList();

        return jdbcTemplate.query(
                getQuery("FIND_BOOKING_IDS_IN_AGENCY"),
                createParams("bookingIds", bookingIds, "agencyId", agencyId),
                (rs, rowNum) -> rs.getLong(1));
    }
}
