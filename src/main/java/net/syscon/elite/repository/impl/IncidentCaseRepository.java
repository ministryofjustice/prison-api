package net.syscon.elite.repository.impl;

import lombok.AllArgsConstructor;
import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.model.Questionnaire;
import net.syscon.elite.api.model.QuestionnaireAnswer;
import net.syscon.elite.api.model.QuestionnaireQuestion;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class IncidentCaseRepository extends RepositoryBase  {

    private final StandardBeanPropertyRowMapper<IncidentCase> INCIDENT_CASE_MAPPER =
            new StandardBeanPropertyRowMapper<>(IncidentCase.class);

    private final StandardBeanPropertyRowMapper<FlatQuestionnaire> QUESTIONNAIRE_MAPPER =
            new StandardBeanPropertyRowMapper<>(FlatQuestionnaire.class);

    public Optional<IncidentCase> getIncidentCase(Long incidentCaseId) {
        Validate.notNull(incidentCaseId, "incidentCaseId is required.");

        try {
            var incidentCase = jdbcTemplate.queryForObject(getQuery("GET_INCIDENT_CASE"),
                    createParams("incidentCaseId", new SqlParameterValue(Types.BIGINT, incidentCaseId)),
                    INCIDENT_CASE_MAPPER);
            return Optional.ofNullable(incidentCase);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
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

    @AllArgsConstructor
    class Tuple {
        String code;
        Long id;
    }

}
