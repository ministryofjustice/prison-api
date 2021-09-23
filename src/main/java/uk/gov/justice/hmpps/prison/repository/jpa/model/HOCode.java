package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"code"}, callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "HO_CODES")
@ToString
public class HOCode extends ExtendedAuditableEntity{

    @Id
    @Column(name = "HO_CODE", nullable = false)
    private String code;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Type(type="yes_no")
    private boolean active;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

}
