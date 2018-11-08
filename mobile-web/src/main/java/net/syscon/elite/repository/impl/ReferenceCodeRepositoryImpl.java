package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.ReferenceDomain;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.ReferenceCodeRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ReferenceCodeRepositoryImpl extends RepositoryBase implements ReferenceCodeRepository {
	private static final StandardBeanPropertyRowMapper<ReferenceDomain> REF_DOMAIN_ROW_MAPPER =
			new StandardBeanPropertyRowMapper<>(ReferenceDomain.class);

	private static final StandardBeanPropertyRowMapper<ReferenceCode> REF_CODE_ROW_MAPPER =
			new StandardBeanPropertyRowMapper<>(ReferenceCode.class);

	private static final StandardBeanPropertyRowMapper<ReferenceCodeDetail> REF_CODE_DETAIL_ROW_MAPPER =
			new StandardBeanPropertyRowMapper<>(ReferenceCodeDetail.class);

	private static final StandardBeanPropertyRowMapper<ReferenceCode> SCHEDULE_REASON_ROW_MAPPER =
	        new StandardBeanPropertyRowMapper<>(ReferenceCode.class);

	@Override
	@Cacheable("referenceDomain")
	public Optional<ReferenceDomain> getReferenceDomain(String domain) {
		String sql = getQuery("FIND_REFERENCE_DOMAIN");

		ReferenceDomain referenceDomain;

		try {
			referenceDomain = jdbcTemplate.queryForObject(
					sql,
					createParams("domain", domain),
					REF_DOMAIN_ROW_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			referenceDomain = null;
		}

		return Optional.ofNullable(referenceDomain);
	}

	@Override
	@Cacheable("referenceCodeByDomainAndCode")
	public Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code, boolean withSubCodes) {
		Optional<ReferenceCode> referenceCode;

		if (withSubCodes) {
			referenceCode = getReferenceCodeWithSubCodesByDomainAndCode(domain, code);
		} else {
			referenceCode = getReferenceCodeByDomainAndCode(domain, code);
		}

		return referenceCode;
	}

	private Optional<ReferenceCode> getReferenceCodeWithSubCodesByDomainAndCode(String domain, String code) {
		String sql = getQuery("FIND_REFERENCE_CODES_BY_DOMAIN_AND_CODE_WITH_CHILDREN");

		List<ReferenceCodeDetail> rcdResults = jdbcTemplate.query(
				sql,
				createParams("domain", domain, "code", code),
				REF_CODE_DETAIL_ROW_MAPPER);

		List<ReferenceCode> referenceCodeAsList = convertToReferenceCodes(rcdResults, false);

		return referenceCodeAsList.isEmpty() ? Optional.empty() : Optional.of(referenceCodeAsList.get(0));
	}

	private Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code) {
		String sql = getQuery("FIND_REFERENCE_CODE_BY_DOMAIN_AND_CODE");

		ReferenceCode referenceCode;

		try {
			referenceCode = jdbcTemplate.queryForObject(
					sql,
					createParams("domain", domain, "code", code),
					REF_CODE_ROW_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			referenceCode = null;
		}

		return Optional.ofNullable(referenceCode);
	}

	@Override
	@Cacheable("referenceCodesByDomain")
	public Page<ReferenceCode> getReferenceCodesByDomain(String domain, boolean withSubCodes, String orderBy, Order order, long offset, long limit) {
		Page<ReferenceCode> page;

        if (withSubCodes) {
            page = getReferenceCodesWithSubCodes(domain, orderBy, order, offset, limit);
        } else {
            page = getReferenceCodes(domain, false, orderBy, order, offset, limit);
        }

		return page;
	}

	private Page<ReferenceCode> getReferenceCodes(String domain, boolean havingSubCodes, String orderBy, Order order, long offset, long limit) {
        String initialSql = getQuery(havingSubCodes ? "FIND_REFERENCE_CODES_BY_DOMAIN_HAVING_SUB_CODES" : "FIND_REFERENCE_CODES_BY_DOMAIN");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, REF_CODE_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addRowCount()
                .addOrderBy(order, orderBy)
                .addPagination()
                .build();

        PageAwareRowMapper<ReferenceCode> paRowMapper = new PageAwareRowMapper<>(REF_CODE_ROW_MAPPER);

        List<ReferenceCode> results = jdbcTemplate.query(
                sql,
                createParams("domain", domain, "offset", offset, "limit", limit),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }

    private Page<ReferenceCode> getReferenceCodesWithSubCodes(String domain, String orderBy, Order order, long offset, long limit) {
		// First, obtain 'parent' codes (but only those that have sub-codes) using specified sorting and pagination
		Page<ReferenceCode> refCodes = getReferenceCodes(domain, true, orderBy, order, offset, limit);

		// Extract codes to list
		List<String> codes = refCodes.getItems().stream().map(ReferenceCode::getCode).collect(Collectors.toList());

		// Build query to obtain sub-codes for domain (as parent domain) and codes (as parent codes) - this query is
		// not paginated as it must get every sub-code for the specified parent domain and codes. It is, however,
		// subject to sorting.
        String initialSql = getQuery("FIND_REFERENCE_CODES_BY_PARENT_DOMAIN_AND_CODE");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, REF_CODE_ROW_MAPPER.getFieldMap());

        String sql = builder
				.addOrderBy(order, orderBy)
				.build();

        List<ReferenceCode> subCodes = jdbcTemplate.query(
                sql,
                createParams("parentDomain", domain, "parentCodes", codes),
				REF_CODE_ROW_MAPPER);

        // Now collect all the sub-codes by parent code
		Map<String, List<ReferenceCode>> collectedSubCodes = collectByParentCode(subCodes);

		// Inject associated sub-codes into each 'parent' reference code
		refCodes.getItems().forEach(rc -> {
			rc.setSubCodes(collectedSubCodes.get(rc.getCode()));
		});

        return new Page<>(refCodes.getItems(), refCodes.getTotalRecords(), offset, limit);
    }

    private Map<String, List<ReferenceCode>> collectByParentCode(List<ReferenceCode> referenceCodes) {
		Map<String, List<ReferenceCode>> refCodeMap = new HashMap<>();

		// Seed map
		List<String> parentCodes = referenceCodes.stream().map(ReferenceCode::getParentCode).distinct().collect(Collectors.toList());

		parentCodes.forEach(pc -> {
			refCodeMap.put(pc, new ArrayList<>());
		});

		// Populate map
		referenceCodes.forEach(rc -> {
			refCodeMap.get(rc.getParentCode()).add(rc);
		});

		return refCodeMap;
	}

	private List<ReferenceCode> convertToReferenceCodes(List<ReferenceCodeDetail> results, boolean suppressEmptySubTypes) {
		List<ReferenceCode> referenceCodes = new ArrayList<>();

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
                        .parentDomain(ref.getParentDomain())
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

	private static void removeWhereSubTypesAreEmpty(List<ReferenceCode> referenceCodes, ReferenceCode activeRef) {
		if (activeRef != null && activeRef.getSubCodes().isEmpty()) {
			referenceCodes.remove(activeRef);
		}
    }

    @Override
    public List<ReferenceCode> getScheduleReasons(String eventType) {
        final String sql = getQuery("GET_AVAILABLE_EVENT_SUBTYPES");
		List<ReferenceCode> scheduledReasons = jdbcTemplate.query(sql, createParams("eventType", eventType), SCHEDULE_REASON_ROW_MAPPER);
		return tidyDescriptionAndSort(scheduledReasons);
    }

    private List<ReferenceCode> tidyDescriptionAndSort(List<ReferenceCode> refCodes) {
        return refCodes.stream()
                .map(p -> ReferenceCode.builder()
                        .code(p.getCode())
                        .description(WordUtils.capitalizeFully(p.getDescription()))
                        .build())
                .sorted(Comparator.comparing(ReferenceCode::getDescription))
                .collect(Collectors.toList());
    }
}
