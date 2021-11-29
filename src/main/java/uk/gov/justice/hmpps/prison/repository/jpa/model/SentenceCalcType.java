package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode( callSuper = false)
@Table(name = "SENTENCE_CALC_TYPES")
@IdClass(SentenceCalcType.PK.class)
@BatchSize(size = 25)
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
    @Type(type="yes_no")
    private boolean active;

}
