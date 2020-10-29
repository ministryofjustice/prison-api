package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCodeInfo;
import uk.gov.justice.hmpps.prison.api.model.ReferenceDomain;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.ReferenceDataRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ReferenceDataRepository extends RepositoryBase {
    private static final StandardBeanPropertyRowMapper<ReferenceDomain> REF_DOMAIN_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(ReferenceDomain.class);

    private static final StandardBeanPropertyRowMapper<ReferenceCode> REF_CODE_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(ReferenceCode.class);

    private static final StandardBeanPropertyRowMapper<ReferenceCodeDetail> REF_CODE_DETAIL_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(ReferenceCodeDetail.class);



    @Cacheable("referenceDomain")
    public Optional<ReferenceDomain> getReferenceDomain(final String domain) {
        final var sql = ReferenceDataRepositorySql.FIND_REFERENCE_DOMAIN.getSql();

        ReferenceDomain referenceDomain;

        try {
            referenceDomain = jdbcTemplate.queryForObject(
                    sql,
                    createParams("domain", domain),
                    REF_DOMAIN_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            referenceDomain = null;
        }

        return Optional.ofNullable(referenceDomain);
    }



    @Cacheable(value = "referenceCodeByDomainAndCode", key = "#domain.concat('-').concat(#code).concat('-').concat(#withSubCodes)")
    public Optional<ReferenceCode> getReferenceCodeByDomainAndCode(final String domain, final String code, final boolean withSubCodes) {
        final Optional<ReferenceCode> referenceCode;

        if (withSubCodes) {
            referenceCode = getReferenceCodeWithSubCodesByDomainAndCode(domain, code);
        } else {
            referenceCode = getReferenceCodeByDomainAndCode(domain, code);
        }

        return referenceCode;
    }

    private Optional<ReferenceCode> getReferenceCodeWithSubCodesByDomainAndCode(final String domain, final String code) {
        final var sql = ReferenceDataRepositorySql.FIND_REFERENCE_CODES_BY_DOMAIN_AND_CODE_WITH_CHILDREN.getSql();

        final var rcdResults = jdbcTemplate.query(
                sql,
                createParams("domain", domain, "code", code),
                REF_CODE_DETAIL_ROW_MAPPER);

        final var referenceCodeAsList = convertToReferenceCodes(rcdResults, false);

        return referenceCodeAsList.isEmpty() ? Optional.empty() : Optional.of(referenceCodeAsList.get(0));
    }

    private Optional<ReferenceCode> getReferenceCodeByDomainAndCode(final String domain, final String code) {
        final var sql = ReferenceDataRepositorySql.FIND_REFERENCE_CODE_BY_DOMAIN_AND_CODE.getSql();

        ReferenceCode referenceCode;

        try {
            referenceCode = jdbcTemplate.queryForObject(
                    sql,
                    createParams("domain", domain, "code", code),
                    REF_CODE_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            referenceCode = null;
        }

        return Optional.ofNullable(referenceCode);
    }


    @CacheEvict(value = "referenceCodeByDomainAndCode", allEntries = true)
    public void insertReferenceCode(final String domain, final String code, final ReferenceCodeInfo referenceCode) {
        final var sql = ReferenceDataRepositorySql.CREATE_REFERENCE_CODE.getSql();
        jdbcTemplate.update(sql, createParams(
                "domain", domain,
                "code", code,
                "description", referenceCode.getDescription(),
                "parentCode", referenceCode.getParentCode(),
                "parentDomain", referenceCode.getParentDomain(),
                "expiredDate", DateTimeConverter.toDate(referenceCode.getExpiredDate()),
                "activeFlag", referenceCode.getActiveFlag(),
                "systemDataFlag", referenceCode.getSystemDataFlag(),
                "listSeq", referenceCode.getListSeq())
        );
    }


    @CacheEvict(value = "referenceCodeByDomainAndCode", allEntries = true)
    public void updateReferenceCode(final String domain, final String code, final ReferenceCodeInfo referenceCode) {
        final var sql = ReferenceDataRepositorySql.UPDATE_REFERENCE_CODE.getSql();
        jdbcTemplate.update(sql, createParams(
                "domain", domain,
                "code", code,
                "description", referenceCode.getDescription(),
                "parentCode", referenceCode.getParentCode(),
                "parentDomain", referenceCode.getParentDomain(),
                "expiredDate", DateTimeConverter.toDate(referenceCode.getExpiredDate()),
                "activeFlag", referenceCode.getActiveFlag(),
                "systemDataFlag", referenceCode.getSystemDataFlag(),
                "listSeq", referenceCode.getListSeq())
        );
    }


    @Cacheable(value = "referenceCodesByDomain")
    public Page<ReferenceCode> getReferenceCodesByDomain(final String domain, final boolean withSubCodes, final String orderBy, final Order order, final long offset, final long limit) {
        final Page<ReferenceCode> page;

        if (withSubCodes) {
            page = getReferenceCodesWithSubCodes(domain, orderBy, order, offset, limit);
        } else {
            page = getReferenceCodes(domain, false, orderBy, order, offset, limit);
        }

        return page;
    }

    private Page<ReferenceCode> getReferenceCodes(final String domain, final boolean havingSubCodes, final String orderBy, final Order order, final long offset, final long limit) {
        final var initialSql = getQuery(havingSubCodes ? "FIND_REFERENCE_CODES_BY_DOMAIN_HAVING_SUB_CODES" : "FIND_REFERENCE_CODES_BY_DOMAIN");

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, REF_CODE_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addRowCount()
                .addOrderBy(order, orderBy)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<ReferenceCode>(REF_CODE_ROW_MAPPER);

        final var results = jdbcTemplate.query(
                sql,
                createParams("domain", domain, "offset", offset, "limit", limit),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }

    private Page<ReferenceCode> getReferenceCodesWithSubCodes(final String domain, final String orderBy, final Order order, final long offset, final long limit) {
        // First, obtain 'parent' codes (but only those that have sub-codes) using specified sorting and pagination
        final var refCodes = getReferenceCodes(domain, true, orderBy, order, offset, limit);

        // Extract codes to list
        final var codes = refCodes.getItems().stream().map(ReferenceCode::getCode).collect(Collectors.toList());

        // Build query to obtain sub-codes for domain (as parent domain) and codes (as parent codes) - this query is
        // not paginated as it must get every sub-code for the specified parent domain and codes. It is, however,
        // subject to sorting.
        final var initialSql = ReferenceDataRepositorySql.FIND_REFERENCE_CODES_BY_PARENT_DOMAIN_AND_CODE.getSql();

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, REF_CODE_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderBy)
                .build();

        final var subCodes = jdbcTemplate.query(
                sql,
                createParams("parentDomain", domain, "parentCodes", codes),
                REF_CODE_ROW_MAPPER);

        // Now collect all the sub-codes by parent code
        final var collectedSubCodes = collectByParentCode(subCodes);

        // Inject associated sub-codes into each 'parent' reference code
        refCodes.getItems().forEach(rc -> {
            rc.setSubCodes(collectedSubCodes.get(rc.getCode()));
        });

        return new Page<>(refCodes.getItems(), refCodes.getTotalRecords(), offset, limit);
    }

    private Map<String, List<ReferenceCode>> collectByParentCode(final List<ReferenceCode> referenceCodes) {
        final Map<String, List<ReferenceCode>> refCodeMap = new HashMap<>();

        // Seed map
        final var parentCodes = referenceCodes.stream().map(ReferenceCode::getParentCode).distinct().collect(Collectors.toList());

        parentCodes.forEach(pc -> {
            refCodeMap.put(pc, new ArrayList<>());
        });

        // Populate map
        referenceCodes.forEach(rc -> {
            refCodeMap.get(rc.getParentCode()).add(rc);
        });

        return refCodeMap;
    }

    private List<ReferenceCode> convertToReferenceCodes(final List<ReferenceCodeDetail> results, final boolean suppressEmptySubTypes) {
        final List<ReferenceCode> referenceCodes = new ArrayList<>();

        ReferenceCode activeRef = null;

        for (final var ref : results) {
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
                        .listSeq(ref.getListSeq())
                        .systemDataFlag(ref.getSystemDataFlag())
                        .expiredDate(ref.getExpiredDate())
                        .build();

                referenceCodes.add(activeRef);
            }

            if (StringUtils.isNotBlank(ref.getSubCode())) {
                activeRef.getSubCodes().add(ReferenceCode.builder()
                        .code(ref.getSubCode())
                        .domain(ref.getSubDomain())
                        .description(ref.getSubDescription())
                        .activeFlag(ref.getSubActiveFlag())
                        .listSeq(ref.getSubListSeq())
                        .systemDataFlag(ref.getSubSystemDataFlag())
                        .expiredDate(ref.getSubExpiredDate())
                        .build());
            }
        }

        if (suppressEmptySubTypes) {
            removeWhereSubTypesAreEmpty(referenceCodes, activeRef);
        }

        return referenceCodes;
    }

    private static void removeWhereSubTypesAreEmpty(final List<ReferenceCode> referenceCodes, final ReferenceCode activeRef) {
        if (activeRef != null && activeRef.getSubCodes().isEmpty()) {
            referenceCodes.remove(activeRef);
        }
    }


    public List<ReferenceCode> getScheduleReasons(final String eventType) {
        final var sql = ReferenceDataRepositorySql.GET_AVAILABLE_EVENT_SUBTYPES.getSql();
        return jdbcTemplate.query(sql, createParams("eventType", eventType), REF_CODE_ROW_MAPPER);
    }
}
