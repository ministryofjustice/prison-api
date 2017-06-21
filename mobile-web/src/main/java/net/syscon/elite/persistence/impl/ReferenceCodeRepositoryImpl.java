package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;
import net.syscon.util.QueryBuilder;
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

	private boolean isAscending(Order order) { return Order.asc.equals(order); }


	@Override
	public ReferenceCode getReferenceCodeByDomainAndCode(String domain, String code) {
		final String sql = getQuery("FIND_REFERENCE_CODE_BY_DOMAIN_CODE");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "code", code), referenceCodeRowMapper);
	}

	@Override
	public ReferenceCode getReferenceCodeByDomainAndParentAndCode(String domain, String parentCode, String code) {
		final String sql = getQuery("FIND_REFERENCE_CODE_BY_DOMAIN_PARENT_CODE");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.queryForObject(sql, createParams("domain", domain, "parentCode", parentCode, "code", code), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getReferenceCodesByDomain(String domain, String query, String orderBy, Order order, int offset, int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_REFERENCE_CODES_BY_DOMAIN"), referenceCodeMapping, preOracle12)
				.addQuery(query)
				.addOrderBy(isAscending(order), orderBy.split(","))
				.addRowCount()
				.addPagedQuery()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("domain", domain, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getReferenceCodesByDomainAndParent(String domain, String parentCode, String query, String orderBy, Order order, int offset, int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_REFERENCE_CODES_BY_DOMAIN_PARENT"), referenceCodeMapping, preOracle12)
				.addQuery(query)
				.addOrderBy(isAscending(order), orderBy.split(","))
				.addRowCount()
				.addPagedQuery()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("domain", domain, "parentCode", parentCode, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypesByCaseLoad(final String caseLoad, final int offset, final int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CNOTE_TYPES_BY_CASELOAD"), referenceCodeMapping, preOracle12)
				.addRowCount()
				.addPagedQuery()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("caseLoad", caseLoad, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}





	// TODO: Remove this method after IG change to the new the endpoint
	@Override
	public List<ReferenceCode> getCnoteSubtypesByCaseNoteType(final String caseNotetype, final int offset, final int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE"), referenceCodeMapping, preOracle12)
				.addRowCount()
				.addPagedQuery()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("caseNoteType", caseNotetype, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}


}
