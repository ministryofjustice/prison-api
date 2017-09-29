package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.persistence.ReferenceCodeRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

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

	private final Map<String, FieldMapper> masterDetailReferenceMapper = new ImmutableMap.Builder<String, FieldMapper>()
			.put("DESCRIPTION", new FieldMapper("description"))
			.put("CODE", new FieldMapper("code"))
			.put("DOMAIN", new FieldMapper("domain"))
			.put("PARENT_DOMAIN", new FieldMapper("parentDomainId"))
			.put("PARENT_CODE", new FieldMapper("parentCode"))
			.put("ACTIVE_FLAG", new FieldMapper("activeFlag"))
			.put("SUB_DOMAIN", new FieldMapper("subDomain"))
			.put("SUB_CODE", new FieldMapper("subCode"))
			.put("SUB_DESCRIPTION", new FieldMapper("subDescription"))
			.put("SUB_ACTIVE_FLAG", new FieldMapper("subActiveFlag"))
			.build();

	private boolean isAscending(Order order) { return Order.ASC == order; }


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
	public List<ReferenceCode> getReferenceCodesByDomain(String domain, String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes) {
		try {
			if (includeSubTypes) {
				final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_REFERENCE_CODES_BY_DOMAIN_PLUS_SUBTYPES"), masterDetailReferenceMapper)
						.build();

				final RowMapper<ReferenceCodeDetail> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCodeDetail.class, masterDetailReferenceMapper);
				final List<ReferenceCodeDetail> results = jdbcTemplate.query(sql, createParams("domain", domain), referenceCodeRowMapper);
				return convertToReferenceCodes(results);

			} else {
				final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_REFERENCE_CODES_BY_DOMAIN"), referenceCodeMapping)
						.addQuery(query)
						.addOrderBy(isAscending(order), orderBy)
						.addRowCount()
						.addPagination()
						.build();
				final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
				return jdbcTemplate.query(sql, createParams("domain", domain, "offset", offset, "limit", limit), referenceCodeRowMapper);
			}
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}

	private List<ReferenceCode> convertToReferenceCodes(List<ReferenceCodeDetail> results) {
		final List<ReferenceCode> referenceCodes = new ArrayList<>();
		ReferenceCode activeRef = null;

		for (ReferenceCodeDetail ref : results) {
            if (activeRef == null || !activeRef.getCode().equalsIgnoreCase(ref.getCode())) {
                activeRef = ReferenceCode.builder()
                        .code(ref.getCode())
                        .domain(ref.getDomain())
                        .description(ref.getDescription())
                        .activeFlag(ref.getActiveFlag())
                        .parentCode(ref.getParentCode())
                        .parentDomainId(ref.getParentDomainId())
                        .subCodes(new ArrayList<>())
                        .build();
                referenceCodes.add(activeRef);
            }
            if (StringUtils.isNotBlank(ref.getSubCode())) {
				activeRef.getSubCodes().add(ReferenceCode.builder()
						.code(ref.getSubCode())
						.domain(ref.getSubDomain())
						.description(ref.getSubDescription())
						.activeFlag(ref.getSubActiveFlag())
						.build());
			}
        }
		return referenceCodes;
	}

	@Override
	public List<ReferenceCode> getReferenceCodesByDomainAndParent(String domain, String parentCode, String query, String orderBy, Order order, long offset, long limit) {
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
	public List<ReferenceCode> getCaseNoteTypeByCurrentCaseLoad(String caseLoadType, boolean includeSubTypes, String query, String orderBy, Order order, long offset, long limit) {

		try {
			if (includeSubTypes) {
				final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_CNOTE_TYPES_AND_SUBTYPES_BY_CASELOAD"), masterDetailReferenceMapper)
						.build();

				final RowMapper<ReferenceCodeDetail> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCodeDetail.class, masterDetailReferenceMapper);
				final List<ReferenceCodeDetail> results = jdbcTemplate.query(sql, createParams("caseLoadType", caseLoadType), referenceCodeRowMapper);
				return convertToReferenceCodes(results);

			} else {
				final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_CNOTE_TYPES_BY_CASELOAD"), referenceCodeMapping)
						.addQuery(query)
						.addOrderBy(isAscending(order), orderBy)
						.addRowCount()
						.addPagination()
						.build();
				final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
				return jdbcTemplate.query(sql, createParams("caseLoadType", caseLoadType, "offset", offset, "limit", limit), referenceCodeRowMapper);
			}
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}


	}
	@Override
	public List<ReferenceCode> getCaseNoteSubType(String typeCode, String query, String orderBy, Order order, long offset, long limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE"), referenceCodeMapping)
				.addQuery(query)
				.addOrderBy(isAscending(order), orderBy)
				.addRowCount()
				.addPagination()
				.build();
		final RowMapper<ReferenceCode> referenceCodeRowMapper = Row2BeanRowMapper.makeMapping(sql, ReferenceCode.class, referenceCodeMapping);
		return jdbcTemplate.query(sql, createParams("caseNoteType", typeCode, "offset", offset, "limit", limit), referenceCodeRowMapper);
	}

}
