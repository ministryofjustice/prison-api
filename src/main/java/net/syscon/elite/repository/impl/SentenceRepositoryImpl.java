package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.OffenceDetail;
import net.syscon.elite.api.model.OffenceHistoryDetail;
import net.syscon.elite.repository.SentenceRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class SentenceRepositoryImpl extends RepositoryBase implements SentenceRepository {

    private final StandardBeanPropertyRowMapper<OffenceDetail> offenceDetailMapper = new StandardBeanPropertyRowMapper<>(OffenceDetail.class);
    private final StandardBeanPropertyRowMapper<OffenceHistoryDetail> offenceHistoryMapper = new StandardBeanPropertyRowMapper<>(OffenceHistoryDetail.class);

    @Override
    public List<OffenceDetail> getMainOffenceDetails(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        final var sql = getQuery("GET_BOOKING_MAIN_OFFENCES");

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "mostSerious", "Y", "chargeStatus", "A", "severityRanking", "999"),
                offenceDetailMapper);
    }

    @Override
    public List<OffenceDetail> getMainOffenceDetails(final List<Long> bookingIds) {
        if (bookingIds.isEmpty()) return Collections.emptyList();

        final var sql = getQuery("GET_BOOKING_MAIN_OFFENCES_MULTIPLE");

        return jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds, "mostSerious", "Y", "chargeStatus", "A"),
                offenceDetailMapper);
    }

    @Override
    public List<OffenceHistoryDetail> getOffenceHistory(final String offenderNo, final boolean convictionsOnly) {
        Objects.requireNonNull(offenderNo, "offenderNo is a required parameter");
        final var sql = getQuery("GET_OFFENCES");

        return jdbcTemplate.query(
                sql,
                createParams("offenderNo", offenderNo, "convictionsOnly", convictionsOnly ? "Y": "N"),
                offenceHistoryMapper);
    }

    @Override
    public Optional<LocalDate> getConfirmedReleaseDate(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        final var sql = getQuery("GET_BOOKING_CONFIRMED_RELEASE_DATE");

        Date releaseDate;

        try {
            releaseDate = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    Date.class);
        } catch (final EmptyResultDataAccessException e) {
            releaseDate = null;
        }

        return Optional.ofNullable(DateTimeConverter.toISO8601LocalDate(releaseDate));
    }
}
