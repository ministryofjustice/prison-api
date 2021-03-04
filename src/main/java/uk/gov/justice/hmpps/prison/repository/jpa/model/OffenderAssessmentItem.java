package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderAssessmentItem.Pk.class)
@Table(name = "OFFENDER_ASSESSMENT_ITEMS")
@ToString(of = {"bookingId", "assessmentSeq", "itemSeq"})
public class OffenderAssessmentItem extends ExtendedAuditableEntity {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Id
    @Column(name = "ASSESSMENT_SEQ")
    private Long assessmentSeq;

    @Id
    @Column(name = "ITEM_SEQ")
    private Long itemSeq;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="ASSESSMENT_ID")
    private AssessmentEntry assessmentAnswer;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Pk implements Serializable {
        private Long bookingId;
        private Long assessmentSeq;
        private Long itemSeq;
    }
}
