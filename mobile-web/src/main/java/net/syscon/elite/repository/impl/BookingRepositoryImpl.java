package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Bookings API repository implementation.
 */
@Repository
@Slf4j
public class BookingRepositoryImpl extends RepositoryBase implements BookingRepository {
    private static final StandardBeanPropertyRowMapper<PrivilegeDetail> PRIV_DETAIL_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(PrivilegeDetail.class);
    private static final StandardBeanPropertyRowMapper<ScheduledEvent> EVENT_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(ScheduledEvent.class);
    private static final StandardBeanPropertyRowMapper<Visit> VISIT_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(Visit.class);
    private static final StandardBeanPropertyRowMapper<OffenderRelease> OFFENDER_RELEASE_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderRelease.class);

    private final Map<String, FieldMapper> sentenceDetailMapping =
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
    public Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_SENTENCE_DETAIL");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, sentenceDetailMapping);
        String sql = builder.build();

        RowMapper<SentenceDetail> sentenceDetailRowMapper =
                Row2BeanRowMapper.makeMapping(sql, SentenceDetail.class, sentenceDetailMapping);

        SentenceDetail sentenceDetail = null;

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
    public List<PrivilegeDetail> getBookingIEPDetails(Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_IEP_DETAILS");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, PRIV_DETAIL_ROW_MAPPER.getFieldMap());
        String sql = builder.build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId),
                PRIV_DETAIL_ROW_MAPPER);
    }

    @Override
    public Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_ACTIVITIES");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addRowCount()
                .addOrderBy(order, orderByFields)
                .addPagination()
                .build();

        PageAwareRowMapper<ScheduledEvent> paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        List<ScheduledEvent> activities = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate)),
                        "offset", offset,
                        "limit", limit),
                paRowMapper);

        return new Page<>(activities, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public List<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_ACTIVITIES");
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
    public Page<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_VISITS");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addRowCount()
                .addOrderBy(order, orderByFields)
                .addPagination()
                .build();

        PageAwareRowMapper<ScheduledEvent> paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        List<ScheduledEvent> visits = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate)),
                        "offset", offset,
                        "limit", limit),
                paRowMapper);

        return new Page<>(visits, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public List<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_VISITS");
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
    public Page<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_APPOINTMENTS");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addRowCount()
                .addOrderBy(order, orderByFields)
                .addPagination()
                .build();

        PageAwareRowMapper<ScheduledEvent> paRowMapper = new PageAwareRowMapper<>(EVENT_ROW_MAPPER);

        List<ScheduledEvent> visits = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId,
                        "fromDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE,  DateTimeConverter.toDate(toDate)),
                        "offset", offset,
                        "limit", limit),
                paRowMapper);

        return new Page<>(visits, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public List<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_APPOINTMENTS");
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
    public Page<OffenderRelease> getOffenderReleaseSummary(LocalDate toReleaseDate, String query, long offset, long limit, String orderByFields, Order order, Set<String> allowedCaseloadsOnly) {
        String initialSql = getQuery("OFFENDER_SUMMARY");
        if (!allowedCaseloadsOnly.isEmpty()) {
            initialSql += " AND EXISTS (select 1 from CASELOAD_AGENCY_LOCATIONS C WHERE ob.AGY_LOC_ID = C.AGY_LOC_ID AND C.CASELOAD_ID IN (:caseloadIds))";
        }
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_RELEASE_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addRowCount()
                .addOrderBy(order, orderByFields)
                .addPagination()
                .addQuery(query)
                .build();

        PageAwareRowMapper<OffenderRelease> paRowMapper = new PageAwareRowMapper<>(OFFENDER_RELEASE_ROW_MAPPER);

        List<OffenderRelease> offenderReleases = jdbcTemplate.query(
                sql,
                createParams("toReleaseDate", DateTimeConverter.toDate(toReleaseDate), "caseloadIds", allowedCaseloadsOnly, "offset", offset, "limit", limit),
                paRowMapper);

        offenderReleases.forEach(or -> or.setInternalLocationDesc(LocationRepositoryImpl.removeAgencyId(or.getInternalLocationDesc(), or.getAgencyLocationId())));
        return new Page<>(offenderReleases, paRowMapper.getTotalRecords(), offset, limit);
    }
}
