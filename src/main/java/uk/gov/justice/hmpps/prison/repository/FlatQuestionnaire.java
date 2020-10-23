package uk.gov.justice.hmpps.prison.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
class FlatQuestionnaire {

    private String code;
    private Long questionnaireId;
    private Long questionnaireQueId;
    private int questionSeq;
    private String questionDesc;
    private int answerSeq;
    private String answerDesc;
    private int questionListSeq;
    private Boolean questionActiveFlag;
    private LocalDateTime questionExpiryDate;
    private Boolean multipleAnswerFlag;
    private Long questionnaireAnsId;
    private Long nextQuestionnaireQueId;
    private int answerListSeq;
    private Boolean answerActiveFlag;
    private LocalDateTime answerExpiryDate;
    private Boolean dateRequiredFlag;
    private Boolean commentRequiredFlag;

}
