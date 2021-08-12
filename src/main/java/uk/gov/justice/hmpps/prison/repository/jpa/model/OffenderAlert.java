package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"offenderBooking", "sequence"}, callSuper = false)
@Table(name = "OFFENDER_ALERTS")
@ToString(exclude = {"offenderBooking"})
@IdClass(OffenderAlert.PK.class)
public class OffenderAlert extends AuditableEntity {
    @Column(name = "ALERT_DATE")
    @CreatedDate
    private LocalDate alertDate;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "ALERT_SEQ")
    private Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AlertType.ALERT_TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "ALERT_TYPE", referencedColumnName = "code"))
    })
    private AlertType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + AlertCode.ALERT_CODE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "ALERT_CODE", referencedColumnName = "code"))
    })
    private AlertCode code;

    @Column(name = "ALERT_STATUS")
    private String status;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @Column(name = "EXPIRY_DATE")
    @CreatedDate
    private LocalDate expiryDate;

    @JoinColumn(name = "CREATE_USER_ID", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private StaffUserAccount createUser;

    @JoinColumn(name = "MODIFY_USER_ID", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private StaffUserAccount modifyUser;

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Integer sequence;
    }
}
