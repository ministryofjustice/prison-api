package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.ReferenceCodeRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReferenceCodeRepositoryImpl extends RepositoryBase implements ReferenceCodeRepository {

    private final StandardBeanPropertyRowMapper<ReferenceCode> referenceCodeMapper =
            new StandardBeanPropertyRowMapper<>(ReferenceCode.class);

    private final StandardBeanPropertyRowMapper<ReferenceCodeDetail> referenceCodeDetailMapper =
            new StandardBeanPropertyRowMapper<>(ReferenceCodeDetail.class);

	@Override
	public Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code, boolean withChildren) {
		ReferenceCode referenceCode;

		if (withChildren) {
			List<ReferenceCode> referenceCodeList = getReferenceCodeByDomainAndCodeWithChildren(domain, code);

			if (referenceCodeList.isEmpty()) {
				referenceCode = null;
			} else {
				referenceCode = referenceCodeList.get(0);
			}
		} else {
			referenceCode = getReferenceCodeByDomainAndCode(domain, code);
		}

		return Optional.ofNullable(referenceCode);
	}

	private List<ReferenceCode> getReferenceCodeByDomainAndCodeWithChildren(String domain, String code) {
		String sql = getQuery("FIND_REFERENCE_CODES_BY_DOMAIN_AND_CODE_WITH_CHILDREN");

		List<ReferenceCodeDetail> rcdResults = jdbcTemplate.query(
				sql,
				createParams("domain", domain, "code", code),
				referenceCodeDetailMapper);

		return convertToReferenceCodes(rcdResults, false);
	}

	private ReferenceCode getReferenceCodeByDomainAndCode(String domain, String code) {
		String sql = getQuery("FIND_REFERENCE_CODE_BY_DOMAIN_CODE");

		ReferenceCode referenceCode;

		try {
			referenceCode = jdbcTemplate.queryForObject(
					sql,
					createParams("domain", domain, "code", code),
					referenceCodeMapper);
		} catch (EmptyResultDataAccessException e) {
			referenceCode = null;
		}

		return referenceCode;
	}

	@Override
	public Optional<ReferenceCode> getReferenceCodeByDomainAndParentAndCode(String domain, String parentCode, String code) {
		String sql = getQuery("FIND_REFERENCE_CODE_BY_DOMAIN_PARENT_CODE");

        ReferenceCode referenceCode;

		try {
            referenceCode = jdbcTemplate.queryForObject(
                    sql,
                    createParams("domain", domain, "parentCode", parentCode, "code", code),
                    referenceCodeMapper);
        } catch (EmptyResultDataAccessException e) {
            referenceCode = null;
        }

        return Optional.ofNullable(referenceCode);
	}

    // TODO: Have to fix this - pagination only applied when not retrieving with sub-types - very confusing.
	@Override
	public Page<ReferenceCode> getReferenceCodesByDomain(String domain, String query, String orderBy, Order order, long offset, long limit, boolean includeSubTypes) {
		Page<ReferenceCode> page;

        if (includeSubTypes) {
            String initialSql = getQuery("FIND_REFERENCE_CODES_BY_DOMAIN_PLUS_SUBTYPES");
            IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, referenceCodeDetailMapper.getFieldMap());
            String sql = builder.build();

            List<ReferenceCodeDetail> rcdResults = jdbcTemplate.query(
                    sql,
                    createParams("domain", domain),
                    referenceCodeDetailMapper);

            List<ReferenceCode> results = convertToReferenceCodes(rcdResults, false);
            page = new Page<>(results, results.size(), 0, results.size());
        } else {
            String initialSql = getQuery("FIND_REFERENCE_CODES_BY_DOMAIN");
            IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, referenceCodeMapper.getFieldMap());

            String sql = builder
                    .addQuery(query)
                    .addOrderBy(order, orderBy)
                    .addRowCount()
                    .addPagination()
                    .build();

            PageAwareRowMapper<ReferenceCode> paRowMapper = new PageAwareRowMapper<>(referenceCodeMapper);

            List<ReferenceCode> results = jdbcTemplate.query(
                    sql,
                    createParams("domain", domain, "offset", offset, "limit", limit),
                    paRowMapper);

            page = new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
        }

		return page;
	}

	private List<ReferenceCode> convertToReferenceCodes(List<ReferenceCodeDetail> results, boolean suppressEmptySubTypes) {
		final List<ReferenceCode> referenceCodes = new ArrayList<>();
		ReferenceCode activeRef = null;

		for (ReferenceCodeDetail ref : results) {
            if (activeRef == null || !activeRef.getCode().equalsIgnoreCase(ref.getCode())) {
				if (suppressEmptySubTypes) {
					removeWhereSubTypesAreEmpty(referenceCodes, activeRef);
				}

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

		if (suppressEmptySubTypes) {
			removeWhereSubTypesAreEmpty(referenceCodes, activeRef);
		}

		return referenceCodes;
	}

	private void removeWhereSubTypesAreEmpty(List<ReferenceCode> referenceCodes, ReferenceCode activeRef) {
		if (activeRef != null && activeRef.getSubCodes().isEmpty()) {
			referenceCodes.remove(activeRef);
		}
	}

	@Override
	public Page<ReferenceCode> getReferenceCodesByDomainAndParent(String domain, String parentCode, String query, String orderBy, Order order, long offset, long limit) {
		String initialSql = getQuery("FIND_REFERENCE_CODES_BY_DOMAIN_PARENT");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, referenceCodeMapper.getFieldMap());

		String sql = builder
				.addQuery(query)
				.addOrderBy(order, orderBy)
				.addRowCount()
				.addPagination()
				.build();

		PageAwareRowMapper<ReferenceCode> paRowMapper = new PageAwareRowMapper<>(referenceCodeMapper);

		List<ReferenceCode> results = jdbcTemplate.query(
		        sql,
                createParams("domain", domain,
                        "parentCode", parentCode,
                        "offset", offset,
                        "limit", limit),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
	}

    // TODO: Remove pagination, not required. Only applied when not retrieving with sub-types - very confusing.
    @Override
    @Cacheable("caseNoteTypeByCurrentCaseLoad")
	public Page<ReferenceCode> getCaseNoteTypeByCurrentCaseLoad(String caseLoadType, boolean includeSubTypes, String query, String orderBy, Order order, long offset, long limit) {
        Page<ReferenceCode> page;

        if (includeSubTypes) {
            // TODO: SQL needs simplifying (eg. left join)
            String initialSql = getQuery("FIND_CNOTE_TYPES_AND_SUBTYPES_BY_CASELOAD");
            IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, referenceCodeDetailMapper.getFieldMap());
            String sql = builder.build();

            List<ReferenceCodeDetail> rcdResults = jdbcTemplate.query(
                    sql,
                    createParams("caseLoadType", caseLoadType),
                    referenceCodeDetailMapper);

            List<ReferenceCode> results = convertToReferenceCodes(rcdResults, true);

            page = new Page<>(results, results.size(), 0, results.size());
        } else {
            String initialSql = getQuery("FIND_CNOTE_TYPES_BY_CASELOAD");
            IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, referenceCodeMapper.getFieldMap());

            String sql = builder
                    .addQuery(query)
                    .addOrderBy(order, orderBy)
                    .addRowCount()
                    .addPagination()
                    .build();

            PageAwareRowMapper<ReferenceCode> paRowMapper = new PageAwareRowMapper<>(referenceCodeMapper);

            List<ReferenceCode> results = jdbcTemplate.query(
                    sql,
                    createParams("caseLoadType", caseLoadType, "offset", offset, "limit", limit),
                    paRowMapper);

            page = new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
        }

        return page;
	}
}
