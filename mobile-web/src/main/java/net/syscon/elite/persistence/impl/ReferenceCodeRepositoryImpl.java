package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;
import net.syscon.util.QueryBuilder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ReferenceCodeRepositoryImpl extends RepositoryBase implements ReferenceCodeRepository {

	private final Map<String, FieldMapper> referenceCodeMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("DESCRIPTION", new FieldMapper("description"))
			.put("CODE", new FieldMapper("code"))
			.put("DOMAIN", new FieldMapper("domain"))
			.put("PARENT_DOMAIN", new FieldMapper("parentDomainId"))
			.put("PARENT_CODE", new FieldMapper("parentCode"))
			.put("ACTIVE_FLAG", new FieldMapper("activeFlag"))
			.build();

	@Override
	public List<ReferenceCode> getCaseNoteTypesByCaseLoad(final String caseLoad, final int offset, final int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CNOTE_TYPES_BY_CASELOAD"), referenceCodeMapping, preOracle12)
				.addRowCount()
				.addPagedQuery()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("caseLoad", caseLoad, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getCaseNoteSubTypesByCaseLoad(final String typeCode, final int offset, final int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE"), referenceCodeMapping, preOracle12)
				.addRowCount()
				.addPagedQuery()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("typeCode", typeCode, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getReferenceCodesByDomain(String domain, int offset, int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_REF_CODES"), referenceCodeMapping, preOracle12).addRowCount().build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("domain", domain), referenceCodeRowMapper);
	}

	@Override
	public ReferenceCode getReferenceCodesByDomainAndCode(String domain, String code) {
		final String sql = getQuery("FIND_REF_CODE_DESC");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "code", code), referenceCodeRowMapper);
	}


	@Override
	public List<ReferenceCode> getAlertTypes(String query, String orderBy, Order order, int offset, int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_REF_CODES"), referenceCodeMapping, preOracle12).addRowCount().build();
		String domain = "ALERT";
		boolean isAsc = order.toString().equals("asc");
		String sqlQuery = new QueryBuilder.Builder(sql, referenceCodeMapping, preOracle12).addQuery(query).addOrderBy(isAsc, orderBy).addPagedQuery().build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sqlQuery, createParams("domain", domain, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}

	@Override
	public ReferenceCode getAlertTypesByAlertType(String alertType) {
		final String sql = getQuery("FIND_REF_CODE_DESC");
		String domain = "ALERT";
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		try {
			return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "code", alertType), referenceCodeRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<ReferenceCode> getAlertTypesByAlertTypeCode(String alertType, String query, String orderBy, Order order, int offset, int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_ALERT_REF_CODES"), referenceCodeMapping, preOracle12).addRowCount().build();
		String domain = "ALERT_CODE";
		boolean isAsc = order.toString().equals("asc");
		String sqlQuery = new QueryBuilder.Builder(sql, referenceCodeMapping, preOracle12).addQuery(query).addOrderBy(isAsc, orderBy).addPagedQuery().build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		try {
			return jdbcTemplate.query(sqlQuery, createParams("domain", domain, "parentCode", alertType, "offset", offset, "limit", limit), referenceCodeRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public ReferenceCode getAlertTypeCodesByAlertCode(String alertType, String alertCode) {
		final String sql = getQuery("FIND_ALERT_REF_CODE_DESC");
		String domain = "ALERT_CODE";
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		try {
			return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "parentCode", alertType, "code", alertCode), referenceCodeRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public ReferenceCode getCaseNoteType(String typeCode) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CASE_NOTE_TYPE_BY_CODE"), referenceCodeMapping, preOracle12).addRowCount().build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql,  createParams("typeCode", typeCode), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CASE_NOTE_TYPES"), referenceCodeMapping, preOracle12)
				.addQuery(query)
				.addRowCount()
				.addPagedQuery()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("offset", offset, "limit", limit), referenceCodeRowMapper);
	}

	@Override
	public ReferenceCode getCaseNoteSubType(String typeCode, String subTypeCode) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CNOTE_SUB_TYPES_BY_TYPECODE_AND_SUBTYPECODE"), referenceCodeMapping, preOracle12).addRowCount().build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql,  createParams("typeCode", typeCode, "subTypeCode", subTypeCode), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getCaseNoteSubTypes(String typeCode, String query, String orderBy, Order order, int offset, int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE"), referenceCodeMapping, preOracle12)
				.addQuery(query)
				.addRowCount()
				.addPagedQuery()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("typeCode", typeCode, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}
}
