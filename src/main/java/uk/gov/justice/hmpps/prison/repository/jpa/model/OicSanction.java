package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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

    @Getter
    public enum OicSanctionCode {
        ADA("Additional Days Added"),
        ASSO("Association/Dine/Recreat/Entertain/Class"),
        ASS_DINING("Dining In Association"),
        ASS_NEWS("Association Newspapers"),
        ASS_SNACKS("Snacks And Beverages On Assoc"),
        BEDDG_OWN("Use Of Own Bedding"),
        BEST_JOBS("Best Jobs"),
        BONUS_PNTS("Bonus Points"),
        CANTEEN("Canteen Facilities"),
        CAUTION("Caution"),
        CC("Cellular Confinement"),
        CELL_ELEC("Electricity In Cell"),
        CELL_FURN("Cell Furniture Above Minimum"),
        COOK_FAC("Cooking Facilities"),
        EXTRA_WORK("Exclusion from Associated Work"),
        EXTW("Extra Work"),
        FOOD_CHOIC("Food Choice On Prison Menu"),
        FORFEIT("Forfeiture of Privileges"),
        GAMES_ELEC("Electronic Games (Access/Hire)"),
        INCOM_TEL("Incoming Telephone Calls"),
        MAIL_ORDER("Use Of Mail Order Facilities"),
        MEAL_TIMES("Meal Times"),
        OIC("Occupations in Cell"),
        OTHER("Other"),
        PADA("Prospective Additional Days Added"),
        PIC("Possessions in Cell"),
        POSSESSION("Possessions (So4/Facil List)"),
        PUBL("Publications"),
        REMACT("Removal from Activity"),
        REMWIN("Removal from Wing/Living Unit"),
        STOP_EARN("Stoppage of Earnings (amount)"),
        STOP_PCT("Stoppage of Earnings (%)"),
        TOBA("Tobacco"),
        USE_FRIDGE("Use Of Fridges And Freezers"),
        USE_GYM("Gymnasium Use"),
        USE_LAUNDRY("Laundry Facilities"),
        USE_LIBRARY("Use Of Library"),
        VISIT_HRS("Visiting Hrs/Evening Visits");

        private final String desc;

        OicSanctionCode(String desc) {
            this.desc = desc;
        }
    }

    public enum Status {
        AS_AWARDED("Activated as Awarded"),
        AWARD_RED("Activated with Quantum Reduced"),
        IMMEDIATE("Immediate"),
        PROSPECTIVE("Prospective"),
        QUASHED("Quashed"),
        REDAPP("Reduced on Appeal"),
        SUSPENDED("Suspended"),
        SUSPEN_EXT("Period of Suspension Extended"),
        SUSPEN_RED("Period of Suspension Shortened"),
        SUSP_PROSP("Suspended and Prospective");

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }
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
