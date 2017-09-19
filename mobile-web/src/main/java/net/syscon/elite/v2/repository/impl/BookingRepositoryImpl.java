package net.syscon.elite.v2.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.impl.RepositoryBase;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.v2.api.model.SentenceDetail;
import net.syscon.elite.v2.repository.BookingRepository;
import net.syscon.util.IQueryBuilder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

/**
 * Bookings API (v2) repository implementation.
 */
@Repository
public class BookingRepositoryImpl extends RepositoryBase implements BookingRepository {
    private final Map<String, FieldMapper> sentenceDetailMapping =
            new ImmutableMap.Builder<String, FieldMapper>()
                    .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
                    .put("START_DATE", new FieldMapper("sentenceStartDate"))
                    .put("END_DATE", new FieldMapper("sentenceEndDate"))
                    .put("CONDITIONAL_RELEASE_DATE", new FieldMapper("conditionalReleaseDate"))
                    .put("AUTOMATIC_RELEASE_DATE", new FieldMapper("automaticReleaseDate"))
                    .put("NON_PAROLE_DATE", new FieldMapper("nonParoleDate"))
                    .put("LICENSE_EXPIRY_DATE", new FieldMapper("licenseExpiryDate"))
                    .put("PAROLE_ELIGIBILITY_DATE", new FieldMapper("paroleEligibilityDate"))
                    .put("HOME_DET_CURF_ELIGIBILITY_DATE", new FieldMapper("homeDetentionCurfewEligibilityDate"))
                    .put("DAYS_REMAINING", new FieldMapper("daysRemaining"))
                    .put("EARLY_TERM_DATE", new FieldMapper("earlyTermDate"))
                    .put("MID_TERM__DATE", new FieldMapper("midTermDate"))
                    .put("LATE_TERM_DATE", new FieldMapper("lateTermDate"))
                    .put("RELEASE_DATE", new FieldMapper("releaseDate"))
                    .build();

    @Override
    public Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId) {
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
}
