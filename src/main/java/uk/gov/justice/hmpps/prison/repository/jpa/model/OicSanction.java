package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@IdClass(OicSanction.PK.class)
@Table(name = "OFFENDER_OIC_SANCTIONS")
public class OicSanction extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID", nullable = false)
        private Long offenderBookId;

        @Column(name = "SANCTION_SEQ", nullable = false)
        private Long sanctionSeq;
    }

    @Id
    private Long offenderBookId;

    @Id
    private Long sanctionSeq;

    @Column(name = "OIC_SANCTION_CODE", length = 12)
    private String oicSanctionCode;

    @Column(name = "COMPENSATION_AMOUNT")
    private Double compensationAmount;

    @Column(name = "SANCTION_MONTHS")
    private Long sanctionMonths;

    @Column(name = "SANCTION_DAYS")
    private Long sanctionDays;

    @Column(name = "COMMENT_TEXT", length = 240)
    private String commentText;

    @Column(name = "EFFECTIVE_DATE", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "APPEALING_DATE")
    private LocalDate appealingDate;

    @Column(name = "CONSECUTIVE_OFFENDER_BOOK_ID")
    private Long consecutiveOffenderBookId;

    @Column(name = "CONSECUTIVE_SANCTION_SEQ")
    private Long consecutiveSanctionSeq;

    @Column(name = "OIC_HEARING_ID")
    private Long oicHearingId;

    @Column(name = "STATUS", length = 12)
    private String status;

    @Column(name = "OFFENDER_ADJUST_ID")
    private Long offenderAdjustId;

    @Column(name = "RESULT_SEQ")
    private Long resultSeq;

    @Column(name = "STATUS_DATE")
    private LocalDate statusDate;

    @Column(name = "OIC_INCIDENT_ID")
    private Long oicIncidentId;

    @Column(name = "LIDS_SANCTION_NUMBER")
    private Long lidsSanctionNumber;
}
