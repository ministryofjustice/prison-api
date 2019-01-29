package net.syscon.elite.repository.impl;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
            .put("EVENT_TYPE",                     new FieldMapper("eventType"))
            .put("OUTCOME_CODE",                   new FieldMapper("outcomeCode"))
            .put("PAY_FLAG",                       new FieldMapper("paid", value -> "Y".equalsIgnoreCase(value.toString())))
            .put("AUTHORISED_ABSENCE_FLAG",        new FieldMapper("authorisedAbsence", value -> "Y".equalsIgnoreCase(value.toString())))
            .build();

    private static final StandardBeanPropertyRowMapper<OffenderSentenceCalculation> SENTENCE_CALC_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderSentenceCalculation.class);

    private static final StandardBeanPropertyRowMapper<Visit> VISIT_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Visit.class);

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
                    .put("ADDITIONAL_DAYS_AWARDED", new FieldMapper("additionalDaysAwarded"))
                    .build();

    private static final Map<String, FieldMapper> SENTENCE_DETAIL_ROW_MAPPER;
    static {
        Map<String, FieldMapper> builderMap = new HashMap<>();
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

    public BookingRepositoryImpl(CreateBookingImpl createBookingRepository, RecallBookingImpl recallBookingRepository) {
        this.createBookingRepository = createBookingRepository;
        this.recallBookingRepository = recallBookingRepository;
    }

    @Override
    @Cacheable("verifyBookingAccess")
    public boolean verifyBookingAccess(Long bookingId, Set<String> agencyIds) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        Objects.requireNonNull(agencyIds, "agencyIds is a required parameter");

        String initialSql = getQuery("CHECK_BOOKING_AGENCIES");

        Long response;

        try {
            log.debug("Verifying access for booking [{}] in caseloads {}", bookingId, agencyIds);

            response = jdbcTemplate.queryForObject(
                    initialSql,
                    createParams("bookingId", bookingId, "agencyIds", agencyIds),
                    Long.class);
        } catch (EmptyResultDataAccessException ex) {
            response = null;
        }

        return bookingId.equals(response);
    }

    @Override
    public boolean checkBookingExists(Long bookingId) {
        log.debug("Verifying booking [{}] exists", bookingId);

        return getBookingAgency(bookingId).isPresent();
    }

    @Override
    @Cacheable("getBookingAgency")
    public Optional<String> getBookingAgency(Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_OFFENDER_BOOKING_AGENCY");
        String agencyId;
        try {
            agencyId = jdbcTemplate.queryForObject(initialSql, createParams("bookingId", bookingId), String.class);
        } catch (EmptyResultDataAccessException ex) {
            agencyId = null;
        }
        return Optional.ofNullable(agencyId);
    }

    @Override
    public Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_SENTENCE_DETAIL");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, SENTENCE_DETAIL_MAPPING);
        String sql = builder.build();

        RowMapper<SentenceDetail> sentenceDetailRowMapper =
                Row2BeanRowMapper.makeMapping(sql, SentenceDetail.class, SENTENCE_DETAIL_MAPPING);

        SentenceDetail sentenceDetail;

        try {
            sentenceDetail =
                    jdbcTemplate.queryForObject(
                            sql,
                            createParams("bookingId", bookingId),
                            sentenceDetailRowMapper);

        } catch (EmptyResultDataAccessException ex) {
            sentenceDetail = null;
        }

        return Optional.ofNullable(sentenceDetail);
    }

    @Override
    public Map<Long, List<PrivilegeDetail>> getBookingIEPDetailsByBookingIds(List<Long> bookingIds) {
        Objects.requireNonNull(bookingIds, "bookingIds are a required parameter");
        List<PrivilegeDetail> privs = jdbcTemplate.query(
                getQuery("GET_BOOKING_IEP_DETAILS_BY_IDS"),
                createParams("bookingIds", bookingIds),
                PRIV_DETAIL_ROW_MAPPER);

        return privs.stream().collect(Collectors.groupingBy(PrivilegeDetail::getBookingId));
    }

    /**
     * @param bookingIds
     * @param cutoffDate Omit alerts which have expired before this date-time
     * @return a list of active alert codes for each booking id
     */
    @Override
    public Map<Long, List<String>> getAlertCodesForBookings(List<Long> bookingIds, LocalDateTime cutoffDate) {
        if (CollectionUtils.isEmpty(bookingIds)) {
            return Collections.emptyMap();
        }
        final List<AlertResult> results = jdbcTemplate.query(
                getQuery("GET_ALERT_CODES_FOR_BOOKINGS"),
                createParams("bookingIds", bookingIds, "cutoffDate", DateTimeConverter.fromLocalDateTime(cutoffDate)),
                ALERTS_MAPPER);

        return results.stream().collect(Collectors.groupingBy(AlertResult::getBookingId,
                Collectors.mapping(AlertResult::getAlertCode, Collectors.toList())));
    }

    @Override
    public Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_ACTIVITIES") + ACTIVITIES_BOOKING_ID_CLAUSE;
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = buildOrderAndPagination(orderByFields, order, builder);

        PageAwareRowMapper<ScheduledEvent> paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        List<ScheduledEvent> activities = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);

        return new Page<>(activities, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public List<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_ACTIVITIES") + ACTIVITIES_BOOKING_ID_CLAUSE;
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<ScheduledEvent> getBookingActivities(Collection<Long> bookingIds, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        String initialSql = getQuery("GET_BOOKING_ACTIVITIES") + ACTIVITIES_BOOKING_ID_IN_CLAUSE;

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public void updateAttendance(Long bookingId, Long activityId, UpdateAttendance updateAttendance, boolean paid, boolean authorisedAbsence) {
        final String sql = getQuery("UPDATE_ATTENDANCE");
        final int rows = jdbcTemplate.update(
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
    public LocalDate getAttendanceEventDate(Long activityId) {
        final Date result;
        try {
            result = jdbcTemplate.queryForObject(
                    getQuery("GET_ATTENDANCE_DATE"),
                    createParams("eventId", activityId),
                    Date.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
        return DateTimeConverter.toISO8601LocalDate(result);
    }

    @Override
    @Cacheable("payableAttendanceOutcomes")
    public PayableAttendanceOutcomeDto getPayableAttendanceOutcome(String eventType, String outcomeCode) {

        Objects.requireNonNull(eventType, "eventType is a required parameter");
        Objects.requireNonNull(outcomeCode, "outcomeCode is a required parameter");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(
                getQuery("GET_PAYABLE_ATTENDANCE_OUTCOMES"),
                PAYABLE_ATTENDANCE_OUTCOMES_MAPPING);
        String sql = builder.build();

        RowMapper<PayableAttendanceOutcomeDto> inmateRowMapper = Row2BeanRowMapper.makeMapping(
                sql, PayableAttendanceOutcomeDto.class, PAYABLE_ATTENDANCE_OUTCOMES_MAPPING);

        return jdbcTemplate.queryForObject(
                sql,
                createParams("eventType", eventType, "outcomeCode", outcomeCode),
                inmateRowMapper);
    }

    @Override
    public Page<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_VISITS") + VISITS_BOOKING_ID_CLAUSE;
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = buildOrderAndPagination(orderByFields, order, builder);

        PageAwareRowMapper<ScheduledEvent> paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        List<ScheduledEvent> visits = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);

        return new Page<>(visits, paRowMapper.getTotalRecords(), offset, limit);
    }

    private String buildOrderAndPagination(String orderByFields, Order order, IQueryBuilder builder) {
        return builder
                .addRowCount()
                .addOrderBy(order, orderByFields)
                .addPagination()
                .build();
    }

    @Override
    public List<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_VISITS") + VISITS_BOOKING_ID_CLAUSE;
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<ScheduledEvent> getBookingVisits(Collection<Long> bookingIds, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        String initialSql = getQuery("GET_BOOKING_VISITS") + VISITS_BOOKING_ID_IN_CLAUSE;
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public Visit getBookingVisitLast(Long bookingId, LocalDateTime cutoffDate) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        Objects.requireNonNull(cutoffDate, "cutoffDate is a required parameter");

        try {
            final Visit result = jdbcTemplate.queryForObject(//
                    getQuery("GET_LAST_BOOKING_VISIT"),
                    createParams("bookingId", bookingId, "cutoffDate", DateTimeConverter.fromLocalDateTime(cutoffDate)),
                    VISIT_ROW_MAPPER);
            result.setLeadVisitor(StringUtils.trimToNull(result.getLeadVisitor()));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Visit getBookingVisitNext(Long bookingId, LocalDateTime from) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        Objects.requireNonNull(from, "from is a required parameter");

        try {
            final Visit result = jdbcTemplate.queryForObject(
                    getQuery("GET_NEXT_BOOKING_VISIT"),
                    createParams("bookingId", bookingId, "fromDate", DateTimeConverter.fromLocalDateTime(from)),
                    VISIT_ROW_MAPPER);
            result.setLeadVisitor(StringUtils.trimToNull(result.getLeadVisitor()));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Cacheable("bookingIdByOffenderNo")
    public Optional<Long> getBookingIdByOffenderNo(String offenderNo) {
        Validate.notBlank("Offender number must be specified.");

        String sql = getQuery("FIND_BOOKING_ID_BY_OFFENDER_NO");

        Long bookingId;

        try {
            bookingId = jdbcTemplate.queryForObject(
                    sql,
                    createParams("offenderNo", offenderNo, "bookingSeq", 1), Long.class);
        } catch (EmptyResultDataAccessException ex) {
            bookingId = null;
        }

        return Optional.ofNullable(bookingId);
    }

    @Override
    public List<OffenderSummary> getBookingsByRelationship(String externalRef, String relationshipType, String identifierType) {

        final String sql = getQuery("FIND_BOOKINGS_BY_PERSON_CONTACT");

        return jdbcTemplate.query(
                sql,
                createParams("identifierType", identifierType,
                        "identifier", externalRef,
                        "relationshipType", relationshipType),
                OFFENDER_SUMMARY_ROW_MAPPER);
    }

    @Override
    public List<OffenderSummary> getBookingsByRelationship(Long personId, String relationshipType) {

        final String sql = getQuery("FIND_BOOKINGS_BY_PERSON_ID_CONTACT");

        return jdbcTemplate.query(
                sql,
                createParams(
                        "personId", personId,
                        "relationshipType", relationshipType),
                OFFENDER_SUMMARY_ROW_MAPPER);
    }

    @Override
    public Page<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_APPOINTMENTS") + APPOINTMENTS_BOOKING_ID_CLAUSE;
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = buildOrderAndPagination(orderByFields, order, builder);

        PageAwareRowMapper<ScheduledEvent> paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        List<ScheduledEvent> visits = jdbcTemplate.query(
                sql,
                buildParams(bookingId, fromDate, toDate, offset, limit),
                paRowMapper);

        return new Page<>(visits, paRowMapper.getTotalRecords(), offset, limit);
    }

    private MapSqlParameterSource buildParams(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit) {
        return createParams("bookingId", bookingId,
                "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
                "offset", offset,
                "limit", limit);
    }

    @Override
    public List<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_APPOINTMENTS") + APPOINTMENTS_BOOKING_ID_CLAUSE;
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<ScheduledEvent> getBookingAppointments(Collection<Long> bookingIds, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingIds, "bookingIds is a required parameter");

        String initialSql = getQuery("GET_BOOKING_APPOINTMENTS") + APPOINTMENTS_BOOKING_ID_IN_CLAUSE;
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public ScheduledEvent getBookingAppointment(Long bookingId, Long eventId) {
        String sql = getQuery("GET_BOOKING_APPOINTMENT");
        return jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "eventId", eventId),
                EVENT_ROW_MAPPER);
    }

    @Override
    public Long createBookingAppointment(Long bookingId, NewAppointment newAppointment, String agencyId) {
        final String sql = getQuery("INSERT_APPOINTMENT");
        final GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        final LocalDateTime startTime = newAppointment.getStartTime();
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
                new String[] {"EVENT_ID"});
        return generatedKeyHolder.getKey().longValue();
    }

    @Override
    public List<OffenderSentenceDetailDto> getOffenderSentenceSummary(String query, Set<String> allowedCaseloadsOnly, boolean filterByCaseload, boolean viewInactiveBookings) {
        String initialSql = getQuery("GET_OFFENDER_SENTENCE_DETAIL");

        var additionSql = new StringBuilder();
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

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, SENTENCE_DETAIL_ROW_MAPPER);

        String sql = builder.addQuery(query).build();

        var offenderSentenceDetailDtoRowMapper = Row2BeanRowMapper.makeMapping(sql, OffenderSentenceDetailDto.class, SENTENCE_DETAIL_ROW_MAPPER);

        return jdbcTemplate.query(
                sql,
                createParams( "caseLoadId", allowedCaseloadsOnly, "activeFlag", "Y", "bookingSeq", 1),
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
    public Optional<OffenderSummary> getLatestBookingByBookingId(Long bookingId) {
        Validate.notNull(bookingId, "Booking id must be specified.");

        String sql = getQuery("GET_LATEST_BOOKING_BY_BOOKING_ID");

        OffenderSummary summary;

        try {
            summary = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    OFFENDER_SUMMARY_ROW_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            summary = null;
        }

        return Optional.ofNullable(summary);
    }

    @Override
    public Optional<OffenderSummary> getLatestBookingByOffenderNo(String offenderNo) {
        Validate.notBlank("Offender number must be specified.");

        String sql = getQuery("GET_LATEST_BOOKING_BY_OFFENDER_NO");

        OffenderSummary summary;

        try {
            summary = jdbcTemplate.queryForObject(
                    sql,
                    createParams("offenderNo", offenderNo),
                    OFFENDER_SUMMARY_ROW_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            summary = null;
        }

        return Optional.ofNullable(summary);
    }

    @Override
    public Long createBooking(String agencyId, NewBooking newBooking) {
        Validate.notNull(newBooking);

        return createBookingRepository.createBooking(agencyId, newBooking);
    }

    @Override
    public Long recallBooking(String agencyId, RecallBooking recallBooking) {
        Validate.notNull(recallBooking);

        return recallBookingRepository.recallBooking(agencyId, recallBooking);
    }

    @Override
    public List<OffenderSentenceCalculation> getOffenderSentenceCalculatons(Set<String> agencyIds) {
        var sql = getQuery("GET_OFFENDER_SENT_CALCULATIONS");
        return jdbcTemplate
                .query(
                sql,
                createParams( "agencyIds", agencyIds, "activeFlag", "Y", "bookingSeq", 1),
                SENTENCE_CALC_ROW_MAPPER);
    }
}
