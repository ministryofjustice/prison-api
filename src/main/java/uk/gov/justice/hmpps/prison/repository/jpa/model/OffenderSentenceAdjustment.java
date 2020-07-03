package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "OFFENDER_SENTENCE_ADJUSTS")
public class OffenderSentenceAdjustment extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_SENTENCE_ADJUST_ID", nullable = false)
    private Long id;

    @Column(name = "SENTENCE_ADJUST_CODE", nullable = false)
    private String sentenceAdjustCode;

    @Column(name = "ADJUST_DAYS")
    private Integer adjustDays;

    @Column(name = "OFFENDER_BOOK_ID", nullable = false)
    private Long offenderBookId;

    @Column(name = "ACTIVE_FLAG")
    @Enumerated(EnumType.STRING)
    private ActiveFlag activeFlag;


    public boolean isActive() {
        return activeFlag != null && activeFlag.isActive();
    }
}
