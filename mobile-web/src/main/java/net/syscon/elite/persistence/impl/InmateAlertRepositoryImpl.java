package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.persistence.InmateAlertRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.util.DateTimeConverter;
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
	public List<Alert> getInmateAlert(long bookingId, String query, String orderByField, Order order, long offset,
									  long limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_INMATE_ALERTS"), alertMapping)
											.addRowCount()
											.addQuery(query)
											.addOrderBy(order == Order.ASC, orderByField)
											.addPagination()
											.build();
		final RowMapper<Alert> alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId, "offset", offset, "limit", limit), alertMapper);
	}

	@Override
	public Optional<Alert> getInmateAlert(long bookingId, long alertSeqId) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_INMATE_ALERT"), alertMapping)
											.build();
		final RowMapper<Alert> alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);

		Alert alert;
		try {
			alert = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "alertSeqId", alertSeqId),alertMapper);
		} catch (EmptyResultDataAccessException e) {
			alert = null;
		}
		return Optional.ofNullable(alert);
	}
}
