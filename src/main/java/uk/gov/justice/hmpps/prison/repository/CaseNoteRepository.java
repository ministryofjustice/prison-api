package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.CaseNoteRepositorySql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

@Repository
@Validated
public class CaseNoteRepository extends RepositoryBase {

    private static final RowMapper<ReferenceCodeDetail> REF_CODE_DETAIL_ROW_MAPPER =
        new StandardBeanPropertyRowMapper<>(ReferenceCodeDetail.class);

    @Cacheable("getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag")
    public List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag(final String caseLoadType, final Boolean active) {
        final var sql = CaseNoteRepositorySql.GET_CASE_NOTE_TYPES_WITH_SUB_TYPES_BY_CASELOAD_TYPE.getSql();

        final var referenceCodeDetails = jdbcTemplate.query(sql,
            createParams("caseLoadType", caseLoadType, "active_flag", active ? "Y" : "N"),
            REF_CODE_DETAIL_ROW_MAPPER);

        return buildCaseNoteTypes(referenceCodeDetails);
    }


    @Cacheable("usedCaseNoteTypesWithSubTypes")
    public List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes() {
        final var sql = CaseNoteRepositorySql.GET_USED_CASE_NOTE_TYPES_WITH_SUB_TYPES.getSql();

        final var referenceCodeDetails = jdbcTemplate.query(sql,
            REF_CODE_DETAIL_ROW_MAPPER);

        return buildCaseNoteTypes(referenceCodeDetails);
    }

    private List<ReferenceCode> buildCaseNoteTypes(final List<ReferenceCodeDetail> results) {
        final Map<String, ReferenceCode> caseNoteTypes = new TreeMap<>();

        results.forEach(ref -> {
            var caseNoteType = caseNoteTypes.get(ref.getCode());

            if (caseNoteType == null) {
                caseNoteType = ReferenceCode.builder()
                    .code(ref.getCode())
                    .domain(ref.getDomain())
                    .description(ref.getDescription())
                    .activeFlag(ref.getActiveFlag())
                    .parentCode(ref.getParentCode())
                    .parentDomain(ref.getParentDomain())
                    .subCodes(new ArrayList<>())
                    .build();

                caseNoteTypes.put(ref.getCode(), caseNoteType);
            }

            if (StringUtils.isNotBlank(ref.getSubCode())) {
                final var caseNoteSubType = ReferenceCode.builder()
                    .code(ref.getSubCode())
                    .domain(ref.getSubDomain())
                    .description(ref.getSubDescription())
                    .activeFlag(ref.getSubActiveFlag())
                    .listSeq(ref.getSubListSeq())
                    .systemDataFlag(ref.getSubSystemDataFlag())
                    .expiredDate(ref.getSubExpiredDate())
                    .build();

                caseNoteType.getSubCodes().add(caseNoteSubType);
            }
        });

        final Predicate<ReferenceCode> typesWithSubTypes = type -> !type.getSubCodes().isEmpty();

        caseNoteTypes.values().stream().filter(typesWithSubTypes).forEach(caseNoteType -> {

            final var sortedSubTypes = caseNoteType.getSubCodes().stream()
                .sorted(Comparator.comparing(a -> a.getDescription().toLowerCase()))
                .toList();

            caseNoteType.setSubCodes(sortedSubTypes);
        });

        return caseNoteTypes.values().stream()
            .filter(typesWithSubTypes)
            .sorted(Comparator.comparing(a -> a.getDescription().toLowerCase()))
            .toList();
    }
}
