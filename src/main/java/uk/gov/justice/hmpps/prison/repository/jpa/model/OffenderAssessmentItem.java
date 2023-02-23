package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderAssessmentItem.Pk.class)
@Table(name = "OFFENDER_ASSESSMENT_ITEMS")
@ToString(of = {"bookingId", "assessmentSeq", "itemSeq"})
public class OffenderAssessmentItem extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Id
    @Column(name = "ASSESSMENT_SEQ")
    private Integer assessmentSeq;

    @Id
    @Column(name = "ITEM_SEQ")
    private Integer itemSeq;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="ASSESSMENT_ID")
    private AssessmentEntry assessmentAnswer;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Pk implements Serializable {
        private Long bookingId;
        private Integer assessmentSeq;
        private Integer itemSeq;
    }
}
