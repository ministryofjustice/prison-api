package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.repository.mapping.*;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InmateAlertRepositoryImpl extends RepositoryBase implements InmateAlertRepository {

	private final Map<String, FieldMapper> alertMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("ALERT_SEQ", 			new FieldMapper("alertId"))
		.put("ALERT_TYPE", 			new FieldMapper("alertType"))
		.put("ALERT_TYPE_DESC", 	new FieldMapper("alertTypeDescription"))
		.put("ALERT_CODE", 			new FieldMapper("alertCode"))
		.put("ALERT_CODE_DESC", 	new FieldMapper("alertCodeDescription"))
		.put("COMMENT_TEXT", 		new FieldMapper("comment", value -> value == null ? "" : value))
		.put("ALERT_DATE", 			new FieldMapper("dateCreated", DateTimeConverter::toISO8601LocalDate))
		.put("EXPIRY_DATE", 		new FieldMapper("dateExpires", DateTimeConverter::toISO8601LocalDate))
		.build();

	@Override
	public Page<Alert> getInmateAlert(long bookingId, String query, String orderByField, Order order, long offset, long limit) {
		String initialSql = getQuery("FIND_INMATE_ALERTS");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, alertMapping);

		String sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order, orderByField)
				.addPagination()
				.build();

		RowMapper<Alert> alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);
        PageAwareRowMapper<Alert> paRowMapper = new PageAwareRowMapper<>(alertMapper);

        List<Alert> results = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "offset", offset, "limit", limit),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
	}

	@Override
	public Optional<Alert> getInmateAlert(long bookingId, long alertSeqId) {
	    String initialSql = getQuery("FIND_INMATE_ALERT");
	    IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, alertMapping);
	    String sql = builder.build();
		RowMapper<Alert> alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);

		Alert alert;

		try {
			alert = jdbcTemplate.queryForObject(
			        sql,
                    createParams("bookingId", bookingId, "alertSeqId", alertSeqId),
                    alertMapper);
		} catch (EmptyResultDataAccessException e) {
			alert = null;
		}

		return Optional.ofNullable(alert);
	}

	public long getAlertCounts(long bookingId, String status) {
        return jdbcTemplate.queryForObject(getQuery("ALERT_COUNTS"),
                createParams("bookingId", bookingId, "status", status),
                Long.class);
	}
}
