package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "OFFENCE_RESULT_CODES")
@EqualsAndHashCode(of = {"code"}, callSuper = false)
@ToString(of = {"code", "description"})
public class OffenceResult extends AuditableEntity {

    @Id
    @Column(name = "RESULT_CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "DISPOSITION_CODE")
    private String dispositionCode;

    @Column(name = "CHARGE_STATUS")
    private String chargeStatus;

    @Column(name = "CONVICTION_FLAG")
    @Type(type="yes_no")
    private boolean convictionFlag;

    @Column(name = "ACTIVE_FLAG")
    @Type(type="yes_no")
    private boolean active;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @Column(name = "LIST_SEQ")
    private Long listSequence;
}
