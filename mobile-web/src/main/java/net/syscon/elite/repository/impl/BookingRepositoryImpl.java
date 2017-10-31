package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.OffenderRelease;
import net.syscon.elite.api.model.PrivilegeDetail;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Bookings API repository implementation.
 */
@Repository
@Slf4j
public class BookingRepositoryImpl extends RepositoryBase implements BookingRepository {
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
                    .put("LICENCE_EXPIRY_DATE", new FieldMapper("licenceExpiryDate", DateTimeConverter::toISO8601LocalDate))
                    .put("PAROLE_ELIGIBILITY_DATE", new FieldMapper("paroleEligibilityDate", DateTimeConverter::toISO8601LocalDate))
                    .put("HOME_DET_CURF_ELIGIBILITY_DATE", new FieldMapper("homeDetentionCurfewEligibilityDate", DateTimeConverter::toISO8601LocalDate))
                    .put("HOME_DET_CURF_APPROVED_DATE", new FieldMapper("homeDetentionCurfewApprovedDate", DateTimeConverter::toISO8601LocalDate))
                    .put("APPROVED_PAROLE_DATE", new FieldMapper("approvedParoleDate", DateTimeConverter::toISO8601LocalDate))
                    .put("RELEASE_ON_TEMP_LICENCE_DATE", new FieldMapper("releaseOnTemporaryLicenceDate", DateTimeConverter::toISO8601LocalDate))
                    .put("EARLY_RELEASE_SCHEME_ELIG_DATE", new FieldMapper("earlyReleaseSchemeEligibilityDate", DateTimeConverter::toISO8601LocalDate))
                    .put("EARLY_TERM_DATE", new FieldMapper("earlyTermDate", DateTimeConverter::toISO8601LocalDate))
                    .put("MID_TERM_DATE", new FieldMapper("midTermDate", DateTimeConverter::toISO8601LocalDate))
                    .put("LATE_TERM_DATE", new FieldMapper("lateTermDate", DateTimeConverter::toISO8601LocalDate))
                    .put("ADDITIONAL_DAYS_AWARDED", new FieldMapper("additionalDaysAwarded"))
                    .build();

    private final StandardBeanPropertyRowMapper<PrivilegeDetail> privilegeDetailMapper = new StandardBeanPropertyRowMapper<>(PrivilegeDetail.class);

    private final StandardBeanPropertyRowMapper<ScheduledEvent> scheduledEventMapper = new StandardBeanPropertyRowMapper<>(ScheduledEvent.class);

    private final StandardBeanPropertyRowMapper<OffenderRelease> offenderReleaseMapper = new StandardBeanPropertyRowMapper<>(OffenderRelease.class);

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
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, privilegeDetailMapper.getFieldMap());
        String sql = builder.build();

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId),
                privilegeDetailMapper);
    }

    @Override
    public Page<ScheduledEvent> getBookingActivities(Long bookingId, long offset, long limit, String orderByFields, Order order) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        String initialSql = getQuery("GET_BOOKING_ACTIVITIES");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, scheduledEventMapper.getFieldMap());
        boolean isAscendingOrder = (order == Order.ASC);

        String sql = builder
                .addRowCount()
                .addOrderBy(isAscendingOrder, orderByFields)
                .addPagination()
                .build();

        PageAwareRowMapper<ScheduledEvent> paRowMapper = new PageAwareRowMapper<>(scheduledEventMapper);

        List<ScheduledEvent> activities = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "offset", offset, "limit", limit),
                paRowMapper);

        return new Page<>(activities, paRowMapper.getTotalRecords(), offset, limit);
    }

    public Page<OffenderRelease> getOffenderReleaseSummary(String query, long offset, long limit, String orderByFields, Order order) {
        String initialSql = getQuery("OFFENDER_SUMMARY");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, offenderReleaseMapper.getFieldMap());
        boolean isAscendingOrder = (order == Order.ASC);

        String sql = builder
                .addRowCount()
                .addOrderBy(isAscendingOrder, orderByFields)
                .addPagination()
                .addQuery(query)
                .build();

        PageAwareRowMapper<OffenderRelease> paRowMapper = new PageAwareRowMapper<>(offenderReleaseMapper);

        List<OffenderRelease> offenderReleases = jdbcTemplate.query(
                sql,
                createParams("offset", offset, "limit", limit),
                paRowMapper);

        offenderReleases.forEach(or -> or.setInternalLocationDesc(LocationRepositoryImpl.removeAgencyId(or.getInternalLocationDesc(), or.getAgencyLocationId())));
        return new Page<>(offenderReleases, paRowMapper.getTotalRecords(), offset, limit);
    }
}
