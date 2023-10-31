package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.api.model.IncidentParty;
import uk.gov.justice.hmpps.prison.api.model.IncidentPartyDto;
import uk.gov.justice.hmpps.prison.api.model.IncidentResponse;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.IncidentCaseRepositorySql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class IncidentCaseRepository extends RepositoryBase {

    private final RowMapper<FlatIncidentCase> INCIDENT_CASE_MAPPER =
            new StandardBeanPropertyRowMapper<>(FlatIncidentCase.class);

    private final RowMapper<IncidentPartyDto> INCIDENT_PARTY_MAPPER =
            new DataClassByColumnRowMapper<>(IncidentPartyDto.class);

    public List<IncidentCase> getIncidentCasesByOffenderNo(final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        final var sql = generateSql(incidentTypes, participationRoles, IncidentCaseRepositorySql.GET_INCIDENT_CASES_BY_OFFENDER_NO);
        final var incidentCaseIds = jdbcTemplate.queryForList(sql,
                createParams("offenderNo", offenderNo, "incidentTypes", incidentTypes, "participationRoles", participationRoles),
                Long.class);
        if (!incidentCaseIds.isEmpty()) {
            return getIncidentCases(incidentCaseIds);
        }

        return Collections.emptyList();
    }

    private String generateSql(final List<String> incidentTypes, final List<String> participationRoles, final IncidentCaseRepositorySql query) {
        var sql = query.getSql();
        if (incidentTypes != null && !incidentTypes.isEmpty()) {
            sql += " AND " + IncidentCaseRepositorySql.FILTER_BY_TYPE.getSql();
        }
        if (participationRoles != null && !participationRoles.isEmpty()) {
            sql += " AND " + IncidentCaseRepositorySql.FILTER_BY_PARTICIPATION.getSql();
        }
        return sql;
    }

    public List<IncidentCase> getIncidentCases(final List<Long> incidentCaseIds) {
        Validate.notEmpty(incidentCaseIds, "incidentCaseIds are required.");

        final var flatIncidentCases = jdbcTemplate.query(IncidentCaseRepositorySql.GET_INCIDENT_CASE.getSql(),
                createParams("incidentCaseIds", incidentCaseIds),
                INCIDENT_CASE_MAPPER);
        final var incidentCases = new ArrayList<IncidentCase>();

        if (!flatIncidentCases.isEmpty()) {
            final var incidentParties = jdbcTemplate.query(IncidentCaseRepositorySql.GET_PARTIES_INVOLVED.getSql(),
                    createParams("incidentCaseIds", incidentCaseIds),
                    INCIDENT_PARTY_MAPPER);

            final var collect = flatIncidentCases.stream()
                    .collect(groupingBy(FlatIncidentCase::getIncidentCaseId));

            collect.forEach((key, value) -> {
                final var responses = new TreeSet<IncidentResponse>();
                final var incidentCaseBuilder = IncidentCase.builder()
                        .responses(responses);

                final var caseId = new AtomicReference<Long>();
                value.forEach(r -> {

                    if (caseId.get() == null || !caseId.get().equals(r.getIncidentCaseId())) {
                        incidentCaseBuilder
                                .incidentCaseId(r.getIncidentCaseId())
                                .incidentDate(r.getIncidentDate())
                                .incidentDetails(r.getIncidentDetails())
                                .incidentStatus(r.getIncidentStatus())
                                .incidentTime(r.getIncidentTime())
                                .incidentTitle(r.getIncidentTitle())
                                .incidentType(r.getIncidentType())
                                .reportDate(r.getReportDate())
                                .reportedStaffId(r.getReportedStaffId())
                                .reportTime(r.getReportTime())
                                .responseLockedFlag(r.getResponseLockedFlag())
                                .agencyId(r.getAgencyId());

                        caseId.set(r.getIncidentCaseId());
                    }

                    responses.add(
                            IncidentResponse.builder()
                                    .answer(r.getAnswer())
                                    .question(r.getQuestion())
                                    .questionnaireAnsId(r.getQuestionnaireAnsId())
                                    .questionnaireQueId(r.getQuestionnaireQueId())
                                    .questionSeq(r.getQuestionSeq())
                                    .recordStaffId(r.getRecordStaffId())
                                    .responseCommentText(r.getResponseCommentText())
                                    .responseDate(r.getResponseDate())
                                    .build());
                });

                incidentCases.add(incidentCaseBuilder.build());
            });

            final var partiesByCase = incidentParties.stream().map(IncidentPartyDto::toIncidentParty).collect(groupingBy(IncidentParty::getIncidentCaseId));

            incidentCases.forEach(ic -> {
                final var parties = partiesByCase.get(ic.getIncidentCaseId());
                if (parties != null) {
                    ic.setParties(new TreeSet<>(parties));
                }
            });
        }

        return incidentCases;
    }
}
