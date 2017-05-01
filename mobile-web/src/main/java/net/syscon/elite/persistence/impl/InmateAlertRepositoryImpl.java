package net.syscon.elite.persistence.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.InmateAlertRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.Alert;

import net.syscon.elite.web.api.resource.BookingResource.Order;
import net.syscon.util.DateFormatProvider;
import net.syscon.util.QueryBuilder;


@Repository
public class InmateAlertRepositoryImpl extends RepositoryBase implements InmateAlertRepository {


	private final Map<String, FieldMapper> alertMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("ALERT_SEQ", 			new FieldMapper("alertId"))
		.put("ALERT_TYPE", 			new FieldMapper("alertType"))
		.put("ALERT_CODE", 				new FieldMapper("alertCode"))
		.put("COMMENT_TEXT", 			new FieldMapper("comment", value -> { System.out.println(value);   return value==null?"":value;}))
		.put("ALERT_DATE", 			new FieldMapper("dateCreated", value -> {return DateFormatProvider.get("yyyy-MM-dd").format((Date)value);}))
		.put("EXPIRY_DATE", 			new FieldMapper("dateExpires", value -> value==null?"":DateFormatProvider.get("yyyy-MM-dd").format((Date)value)))
		.build();

	@Override
	public List<Alert> getInmateAlert(String bookingId, String query, String orderByField, Order order, int offset,
			int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_INMATE_ALERTS"), alertMapping)
											.addQuery(query)
											.addOrderBy("asc".equalsIgnoreCase(order.toString())?true:false, orderByField)
											.addPagedQuery()
											.build();
		final RowMapper<Alert> alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId, "offset", offset, "limit", limit), alertMapper);
	}

	@Override
	public Alert getInmateAlert(String bookingId, String alertSeqId) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_INMATE_ALERT"), alertMapping)
											.build();
		final RowMapper<Alert> alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);
		return jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "alertSeqId", alertSeqId),alertMapper);
	}
	
	

}
