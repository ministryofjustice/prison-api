package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "OFFENCE_RESULT_CODES")
@EqualsAndHashCode(of = {"code"}, callSuper = false)
@ToString(of = {"code", "description"})
@With
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
    @Convert(converter = YesNoConverter.class)
    private boolean convictionFlag;

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @Column(name = "LIST_SEQ")
    private Long listSequence;
}
