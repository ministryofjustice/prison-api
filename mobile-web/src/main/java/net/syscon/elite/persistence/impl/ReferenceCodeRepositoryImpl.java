package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.CaseLoad;
import net.syscon.elite.web.api.model.CaseNoteType;
import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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
	
	private final Map<String, FieldMapper> noteTypesSubTypes = new ImmutableMap.Builder<String, FieldMapper>()
			.put("DESCRIPTION", new FieldMapper("description"))
			.put("CODE", new FieldMapper("code"))
			.build();

	private boolean isAscending(Order order) { return Order.asc.equals(order); }
	private boolean isAscending(String order) {return "asc".equalsIgnoreCase(order);}


	@Override
	public Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code) {
		final String sql = getQuery("FIND_REFERENCE_CODE_BY_DOMAIN_CODE");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, createParams("domain", domain, "code", code), referenceCodeRowMapper));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<ReferenceCode> getReferenceCodeByDomainAndParentAndCode(String domain, String parentCode, String code) {
		final String sql = getQuery("FIND_REFERENCE_CODE_BY_DOMAIN_PARENT_CODE");
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, createParams("domain", domain, "parentCode", parentCode, "code", code), referenceCodeRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
	}

	@Override
	public List<ReferenceCode> getReferenceCodesByDomain(String domain, String query, String orderBy, Order order, int offset, int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_REFERENCE_CODES_BY_DOMAIN"), referenceCodeMapping)
				.addQuery(query)
				.addOrderBy(isAscending(order), orderBy)
				.addRowCount()
				.addPagination()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
        try {
            return jdbcTemplate.query(sql, createParams("domain", domain, "offset", offset, "limit", limit), referenceCodeRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
	}

	@Override
	public List<ReferenceCode> getReferenceCodesByDomainAndParent(String domain, String parentCode, String query, String orderBy, Order order, int offset, int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_REFERENCE_CODES_BY_DOMAIN_PARENT"), referenceCodeMapping)
				.addQuery(query)
				.addOrderBy(isAscending(order), orderBy)
				.addRowCount()
				.addPagination()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
        try {
            return jdbcTemplate.query(sql, createParams("domain", domain, "parentCode", parentCode, "offset", offset, "limit", limit), referenceCodeRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
	}

	@Override
	public List<CaseNoteType> getCaseNoteTypeByCurrentCaseLoad(String query, String orderBy, String order, int offset, int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_CNOTE_TYPES_BY_CASELOAD"), noteTypesSubTypes)
				.addQuery(query)
				.addOrderBy(isAscending(order), orderBy)
				.addRowCount()
				.addPagination()
				.build();
		final RowMapper<CaseNoteType> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNoteType.class, noteTypesSubTypes);
        try {
			final Optional<CaseLoad> caseLoad = getCurrentCaseLoadDetail();
			final String caseLoadType = caseLoad.isPresent() ? caseLoad.get().getType() : "BOTH";
			return jdbcTemplate.query(sql, createParams("caseLoadType", caseLoadType, "offset", offset, "limit", limit), referenceCodeRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
	}
	@Override
	public List<CaseNoteType> getCaseNoteSubType(String typeCode, String query, String orderBy, String order, int offset, int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE"), noteTypesSubTypes)
				.addQuery(query)
				.addOrderBy(isAscending(order), orderBy)
				.addRowCount()
				.addPagination()
				.build();
		final RowMapper<CaseNoteType> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNoteType.class, noteTypesSubTypes);
		return jdbcTemplate.query(sql, createParams("caseNoteType", typeCode, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}


}
