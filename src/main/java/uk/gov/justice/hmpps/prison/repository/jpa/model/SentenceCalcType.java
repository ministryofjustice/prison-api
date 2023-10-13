package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.type.YesNoConverter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Entity
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
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

    public boolean isAFine() {
        return A_FINE_TYPE.equals(calculationType);
    }

    public boolean isFixedTermRecallType() {
        return FIXED_TERM_RECALL_TYPES.contains(calculationType);
    }

    private static final Set<String> FIXED_TERM_RECALL_TYPES = Set.of("14FTR_ORA", "FTR_14_ORA", "FTR", "FTR_ORA", "FTR_SCH15", "FTRSCH15_ORA", "FTRSCH18", "FTRSCH18_ORA");
    private static final String A_FINE_TYPE = "A/FINE";

}
