package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.OffenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceDetailDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetailDto;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.SentenceRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SentenceRepository extends RepositoryBase {

    private final RowMapper<OffenceDetailDto> offenceDetailMapper = new DataClassByColumnRowMapper<>(OffenceDetailDto.class);
    private final RowMapper<OffenceHistoryDetailDto> offenceHistoryMapper = new DataClassByColumnRowMapper<>(OffenceHistoryDetailDto.class);

    public List<OffenceDetail> getMainOffenceDetails(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        final var sql = SentenceRepositorySql.GET_BOOKING_MAIN_OFFENCES.getSql();

        final var offences = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "mostSerious", "Y", "chargeStatus", "A", "severityRanking", "999"),
                offenceDetailMapper);
        return offences.stream().map(OffenceDetailDto::toOffenceDetail).collect(Collectors.toList());
    }


    public List<OffenceDetail> getMainOffenceDetails(final List<Long> bookingIds) {
        if (bookingIds.isEmpty()) return Collections.emptyList();

        final var sql = SentenceRepositorySql.GET_BOOKING_MAIN_OFFENCES_MULTIPLE.getSql();

        final var offences = jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds, "mostSerious", "Y", "chargeStatus", "A"),
                offenceDetailMapper);
        return offences.stream().map(OffenceDetailDto::toOffenceDetail).collect(Collectors.toList());
    }


    public List<OffenceHistoryDetail> getOffenceHistory(final String offenderNo, final boolean convictionsOnly) {
        Objects.requireNonNull(offenderNo, "offenderNo is a required parameter");
        final var sql = SentenceRepositorySql.GET_OFFENCES.getSql();

        final var offences = jdbcTemplate.query(
                sql,
                createParams("offenderNo", offenderNo, "convictionsOnly", convictionsOnly ? "Y": "N"),
                offenceHistoryMapper);
        return offences.stream().map(OffenceHistoryDetailDto::toOffenceHistoryDetail).collect(Collectors.toList());
    }


    public List<OffenceHistoryDetail> getActiveOffencesForBooking(final Long bookingId, final boolean convictionsOnly) {
        Objects.requireNonNull(bookingId, "offenderNo is a required parameter");
        final var sql = SentenceRepositorySql.GET_OFFENCES_FOR_BOOKING.getSql();

        final var offences = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "convictionsOnly", convictionsOnly ? "Y": "N"),
                offenceHistoryMapper);
        return offences.stream().map(OffenceHistoryDetailDto::toOffenceHistoryDetail).collect(Collectors.toList());
    }


    public Optional<LocalDate> getConfirmedReleaseDate(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        final var sql = SentenceRepositorySql.GET_BOOKING_CONFIRMED_RELEASE_DATE.getSql();

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
