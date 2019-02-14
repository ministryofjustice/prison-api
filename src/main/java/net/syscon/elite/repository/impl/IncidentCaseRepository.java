package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class IncidentCaseRepository extends RepositoryBase {

    private final StandardBeanPropertyRowMapper<FlatIncidentCase> INCIDENT_CASE_MAPPER =
            new StandardBeanPropertyRowMapper<>(FlatIncidentCase.class);

    private final StandardBeanPropertyRowMapper<IncidentParty> INCIDENT_PARTY_MAPPER =
            new StandardBeanPropertyRowMapper<>(IncidentParty.class);


    private final StandardBeanPropertyRowMapper<FlatQuestionnaire> QUESTIONNAIRE_MAPPER =
            new StandardBeanPropertyRowMapper<>(FlatQuestionnaire.class);


    public List<IncidentCase> getIncidentCasesByOffenderNo(String offenderNo, String incidentType, List<String> participationRoles) {
        String sql = getQuery("GET_INCIDENT_CASES_BY_OFFENDER_NO");

        if (StringUtils.isNotBlank(incidentType)) {
            sql += " AND " + getQuery("FILTER_BY_TYPE");
        }
        if (participationRoles != null && !participationRoles.isEmpty()) {
            sql += " AND " + getQuery("FILTER_BY_PARTICIPATION");
        }
        var incidentCaseIds = jdbcTemplate.queryForList(sql,
                createParams("offenderNo", offenderNo, "incidentType", incidentType, "participationRoles", participationRoles),
                Long.class);
        return getIncidentCases(incidentCaseIds);

    }

    public List<IncidentCase> getIncidentCasesByBookingId(Long bookingId, String incidentType, List<String> participationRoles) {
        String sql = getQuery("GET_INCIDENT_CASES_BY_BOOKING_ID");
        var incidentCaseIds = jdbcTemplate.queryForList(sql,
                createParams("bookingId", bookingId, "incidentType", incidentType, "participationRoles", participationRoles),
                Long.class);

        return getIncidentCases(incidentCaseIds);
    }

    public List<IncidentCase> getIncidentCases(List<Long> incidentCaseIds) {
        Validate.notNull(incidentCaseIds, "incidentCaseIds are required.");

        var flatIncidentCases = jdbcTemplate.query(getQuery("GET_INCIDENT_CASE"),
                createParams("incidentCaseIds", incidentCaseIds),
                INCIDENT_CASE_MAPPER);
        final var incidentCases = new ArrayList<IncidentCase>();

        if (flatIncidentCases.size() > 0) {
            var incidentParties = jdbcTemplate.query(getQuery("GET_PARTIES_INVOLVED"),
                    createParams("incidentCaseIds", incidentCaseIds),
                    INCIDENT_PARTY_MAPPER);

            Map<Long, List<FlatIncidentCase>> collect = flatIncidentCases.stream()
                    .collect(groupingBy(FlatIncidentCase::getIncidentCaseId));

            collect.forEach((key, value) -> {
                var responses = new TreeSet<IncidentResponse>();
                var incidentCaseBuilder = IncidentCase.builder()
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
                ic.setParties(new TreeSet<>(partiesByCase.get(ic.getIncidentCaseId())));
            });
        }

        return incidentCases;
    }

    public Optional<Questionnaire> getQuestionnaire(String category, String code) {

        var questionnaireData = jdbcTemplate.query(getQuery("QUESTIONNAIRE"),
                createParams(
                        "category", category,
                        "code", code),
                QUESTIONNAIRE_MAPPER);

        if (!questionnaireData.isEmpty()) {
            var collect = questionnaireData.stream()
                    .collect(groupingBy(FlatQuestionnaire::getQuestionnaireId,
                            groupingBy(FlatQuestionnaire::getQuestionnaireQueId)));

            Questionnaire.QuestionnaireBuilder questionnaireBuilder = Questionnaire.builder().code(code);

            collect.forEach((key, value) -> {
                var questions = new TreeSet<QuestionnaireQuestion>();
                questionnaireBuilder.questionnaireId(key);
                questionnaireBuilder.questions(questions);

                final var quesId = new AtomicReference<Long>();
                value.forEach((k, v) -> {
                    var answers = new TreeSet<QuestionnaireAnswer>();

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

}
