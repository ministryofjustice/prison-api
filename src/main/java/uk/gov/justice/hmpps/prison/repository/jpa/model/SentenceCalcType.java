package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode( callSuper = false)
@Table(name = "SENTENCE_CALC_TYPES")
@IdClass(SentenceCalcType.PK.class)
public class SentenceCalcType extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private String calculationType;
        private String category;
    }

    @Id
    @Column(name = "SENTENCE_CALC_TYPE")
    private String calculationType;

    @Id
    @Column(name = "SENTENCE_CATEGORY")
    private String category;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PROGRAM_METHOD")
    private String programMethod;

    @Column(name = "FUNCTION_TYPE")
    private String functionType;

    @Column(name = "LIST_SEQ")
    private Long listSequence;

    @Column(name = "REORDER_SENTENCE_SEQ")
    private Long reorderSentenceSequence;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    public Boolean isAFine() {
        return Objects.equals(calculationType, "A/FINE");
    }

    public Boolean isRecallType(){
        var licenceRecallTypes = new String[]{"LR", "LR_ORA", "LR_YOI_ORA", "LR_SEC91_ORA", "LRSEC250_ORA"};
        return Arrays.stream(licenceRecallTypes).filter(Objects::nonNull).anyMatch(licenceRecallType -> licenceRecallType.equals(calculationType));
    }

}
