package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.api.model.IncidentParty;
import uk.gov.justice.hmpps.prison.api.model.IncidentResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.api.model.Questionnaire;
import uk.gov.justice.hmpps.prison.api.model.QuestionnaireAnswer;
import uk.gov.justice.hmpps.prison.api.model.QuestionnaireQuestion;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.IncidentCaseRepositorySql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class IncidentCaseRepository extends RepositoryBase {

    private final StandardBeanPropertyRowMapper<FlatIncidentCase> INCIDENT_CASE_MAPPER =
            new StandardBeanPropertyRowMapper<>(FlatIncidentCase.class);

    private final StandardBeanPropertyRowMapper<IncidentParty> INCIDENT_PARTY_MAPPER =
            new StandardBeanPropertyRowMapper<>(IncidentParty.class);

    private final StandardBeanPropertyRowMapper<FlatQuestionnaire> QUESTIONNAIRE_MAPPER =
            new StandardBeanPropertyRowMapper<>(FlatQuestionnaire.class);

    private final StandardBeanPropertyRowMapper<OffenderSummary> CANDIDATE_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderSummary.class);

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

    public List<IncidentCase> getIncidentCasesByBookingId(final Long bookingId, final List<String> incidentTypes, final List<String> participationRoles) {
        final var sql = generateSql(incidentTypes, participationRoles, IncidentCaseRepositorySql.GET_INCIDENT_CASES_BY_BOOKING_ID);

        final var incidentCaseIds = jdbcTemplate.queryForList(sql,
                createParams("bookingId", bookingId, "incidentTypes", incidentTypes, "participationRoles", participationRoles),
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

        if (flatIncidentCases.size() > 0) {
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

            final var partiesByCase = incidentParties.stream().collect(groupingBy(IncidentParty::getIncidentCaseId));

            incidentCases.forEach(ic -> {
                final var parties = partiesByCase.get(ic.getIncidentCaseId());
                if (parties != null) {
                    ic.setParties(new TreeSet<>(parties));
                }
            });
        }

        return incidentCases;
    }

    public Optional<Questionnaire> getQuestionnaire(final String category, final String code) {

        final var questionnaireData = jdbcTemplate.query(IncidentCaseRepositorySql.QUESTIONNAIRE.getSql(),
                createParams(
                        "category", category,
                        "code", code),
                QUESTIONNAIRE_MAPPER);

        if (!questionnaireData.isEmpty()) {
            final var collect = questionnaireData.stream()
                    .collect(groupingBy(FlatQuestionnaire::getQuestionnaireId,
                            groupingBy(FlatQuestionnaire::getQuestionnaireQueId)));

            final var questionnaireBuilder = Questionnaire.builder().code(code);

            collect.forEach((key, value) -> {
                final var questions = new TreeSet<QuestionnaireQuestion>();
                questionnaireBuilder.questionnaireId(key);
                questionnaireBuilder.questions(questions);

                final var quesId = new AtomicReference<Long>();
                value.forEach((k, v) -> {
                    final var answers = new TreeSet<QuestionnaireAnswer>();

                    v.forEach(q -> {
                        if (quesId.get() == null || !quesId.get().equals(q.getQuestionnaireQueId())) {
                            questions.add(QuestionnaireQuestion.builder()
                                    .questionDesc(q.getQuestionDesc())
                                    .multipleAnswerFlag(q.getMultipleAnswerFlag())
                                    .nextQuestionnaireQueId(q.getNextQuestionnaireQueId())
                                    .questionActiveFlag(q.getQuestionActiveFlag())
                                    .questionExpiryDate(q.getQuestionExpiryDate())
                                    .questionListSeq(q.getQuestionListSeq())
                                    .questionnaireQueId(q.getQuestionnaireQueId())
                                    .questionSeq(q.getQuestionSeq())
                                    .answers(answers)
                                    .build());

                            quesId.set(q.getQuestionnaireQueId());
                        }

                        answers.add(QuestionnaireAnswer.builder()
                                .answerDesc(q.getAnswerDesc())
                                .answerActiveFlag(q.getAnswerActiveFlag())
                                .answerExpiryDate(q.getAnswerExpiryDate())
                                .answerListSeq(q.getAnswerListSeq())
                                .answerSeq(q.getAnswerSeq())
                                .commentRequiredFlag(q.getCommentRequiredFlag())
                                .dateRequiredFlag(q.getDateRequiredFlag())
                                .questionnaireAnsId(q.getQuestionnaireAnsId())
                                .build());
                    });

                });

            });
            return Optional.of(questionnaireBuilder.build());
        }

        return Optional.empty();

    }

    public Page<String> getIncidentCandidates(LocalDateTime cutoffTimestamp, final long offset, final long limit) {
        final var builder = queryBuilderFactory.getQueryBuilder(IncidentCaseRepositorySql.GET_INCIDENT_CANDIDATES.getSql(), CANDIDATE_MAPPER);

        final var sql = builder
                .addRowCount()
                .addPagination()
                .build();

        final var rowMapper = Row2BeanRowMapper.makeMapping(OffenderSummary.class, CANDIDATE_MAPPER.getFieldMap());
        final var paRowMapper = new PageAwareRowMapper<>(rowMapper);

        final List<OffenderSummary> offenderSummaries = jdbcTemplate.query(
                sql,
                createParams("cutoffTimestamp", cutoffTimestamp, "offset", offset, "limit", limit),
                paRowMapper);
        final var results = offenderSummaries.stream().map(OffenderSummary::getOffenderNo).toList();

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }
}
