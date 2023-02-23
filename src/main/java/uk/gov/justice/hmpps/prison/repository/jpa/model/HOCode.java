package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"code"}, callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "HO_CODES")
@ToString
public class HOCode extends AuditableEntity{

    @Id
    @Column(name = "HO_CODE", nullable = false)
    private String code;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

}
