package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@IdClass(OicSanction.PK.class)
@Table(name = "OFFENDER_OIC_SANCTIONS")
public class OicSanction extends AuditableEntity {

    @Getter
    public enum OicSanctionCode {
        ADA,
        ASSO,
        ASS_DINING,
        ASS_NEWS,
        ASS_SNACKS,
        BEDDG_OWN,
        BEST_JOBS,
        BONUS_PNTS,
        CANTEEN,
        CAUTION,
        CC,
        CELL_ELEC,
        CELL_FURN,
        COOK_FAC,
        EXTRA_WORK,
        EXTW,
        FOOD_CHOIC,
        FORFEIT,
        GAMES_ELEC,
        INCOM_TEL,
        MAIL_ORDER,
        MEAL_TIMES,
        OIC,
        OTHER,
        PADA,
        PIC,
        POSSESSION,
        PUBL,
        REMACT,
        REMWIN,
        STOP_EARN,
        STOP_PCT,
        TOBA,
        USE_FRIDGE,
        USE_GYM,
        USE_LAUNDRY,
        USE_LIBRARY,
        VISIT_HRS;
    }

    public enum Status {
        AS_AWARDED,
        AWARD_RED,
        IMMEDIATE,
        PROSPECTIVE,
        QUASHED,
        REDAPP,
        SUSPENDED,
        SUSPEN_EXT,
        SUSPEN_RED,
        SUSP_PROSP;
    }

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

    @Enumerated(EnumType.STRING)
    @Column(name = "OIC_SANCTION_CODE", length = 12)
    private OicSanctionCode oicSanctionCode;

    @Digits(integer=11, fraction=2)
    @Column(name = "COMPENSATION_AMOUNT")
    private BigDecimal compensationAmount;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 12)
    private Status status;

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
