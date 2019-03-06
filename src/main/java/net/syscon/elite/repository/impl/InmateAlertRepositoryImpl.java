package net.syscon.elite.repository.impl;

import com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.util.DateTimeConverter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InmateAlertRepositoryImpl extends RepositoryBase implements InmateAlertRepository {

    private final Map<String, FieldMapper> alertMapping = new ImmutableMap.Builder<String, FieldMapper>()
        .put("ALERT_SEQ", new FieldMapper("alertId"))
        .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
        .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
        .put("ALERT_TYPE", new FieldMapper("alertType"))
        .put("ALERT_TYPE_DESC", new FieldMapper("alertTypeDescription"))
        .put("ALERT_CODE", new FieldMapper("alertCode"))
        .put("ALERT_CODE_DESC", new FieldMapper("alertCodeDescription"))
        .put("COMMENT_TEXT", new FieldMapper("comment", value -> value == null ? "" : value))
        .put("ALERT_STATUS", new FieldMapper("active", "ACTIVE"::equals))
        .put("ALERT_DATE", new FieldMapper("dateCreated", DateTimeConverter::toISO8601LocalDate))
        .put("EXPIRY_DATE", new FieldMapper("dateExpires", DateTimeConverter::toISO8601LocalDate))
        .put("ADD_FIRST_NAME", new FieldMapper("addedByFirstName"))
        .put("ADD_LAST_NAME", new FieldMapper("addedByLastName"))
        .put("UPDATE_FIRST_NAME", new FieldMapper("expiredByFirstName"))
        .put("UPDATE_LAST_NAME", new FieldMapper("expiredByLastName"))
        .build();

	@Override
    public Page<Alert> getInmateAlerts(final long bookingId, final String query, final String orderByField, final Order order, final long offset, final long limit) {
        final var initialSql = getQuery("FIND_INMATE_ALERTS");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, alertMapping);

        final var sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order, orderByField)
				.addPagination()
				.build();

        final var alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);
        final var paRowMapper = new PageAwareRowMapper<Alert>(alertMapper);

        final var results = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "offset", offset, "limit", limit),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
	}

	@Override
    public Optional<Alert> getInmateAlerts(final long bookingId, final long alertSeqId) {
        final var initialSql = getQuery("FIND_INMATE_ALERT");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, alertMapping);
        final var sql = builder.build();
        final var alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);

		Alert alert;

		try {
			alert = jdbcTemplate.queryForObject(
			        sql,
                    createParams("bookingId", bookingId, "alertSeqId", alertSeqId),
                    alertMapper);
        } catch (final EmptyResultDataAccessException e) {
			alert = null;
		}

		return Optional.ofNullable(alert);
	}

    @Override
    public List<Alert> getInmateAlertsByOffenderNos(final String agencyId, final List<String> offenderNos) {
        final var sql = getQuery("FIND_INMATE_OFFENDERS_ALERTS");
        final var alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);
        return jdbcTemplate.query(
                sql,
                createParams(
                        "offenderNos", offenderNos,
                        "agencyId", agencyId),
                alertMapper);
    }
}
