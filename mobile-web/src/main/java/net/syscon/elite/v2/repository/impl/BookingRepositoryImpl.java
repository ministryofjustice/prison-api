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
                    .put("START_DATE", new FieldMapper("sentenceStartDate"))
                    .put("END_DATE", new FieldMapper("sentenceEndDate"))
                    .build();

    @Override
    public Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId) {
//        String initialSql = getQuery("GET_BOOKING_SENTENCE_DETAIL");
//        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, sentenceDetailMapping);
//        String sql = builder.build();
//
//        RowMapper<SentenceDetail> sentenceDetailRowMapper =
//                Row2BeanRowMapper.makeMapping(sql, SentenceDetail.class, sentenceDetailMapping);
//
        SentenceDetail sentenceDetail = null;

//        try {
//            sentenceDetail =
//                    jdbcTemplate.queryForObject(
//                            sql,
//                            createParams("bookingId", bookingId),
//                            sentenceDetailRowMapper);
//
//        } catch (EmptyResultDataAccessException ex) {
//            sentenceDetail = null;
//        }

        return Optional.ofNullable(sentenceDetail);
    }
}
