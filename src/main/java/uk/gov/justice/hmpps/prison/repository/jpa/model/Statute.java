package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
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
import jakarta.persistence.Table;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"code"}, callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "STATUTES")
@ToString
@With
public class Statute extends AuditableEntity{

    @Id
    @Column(name = "STATUTE_CODE", nullable = false)
    private String code;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "LEGISLATING_BODY_CODE", nullable = false)
    private String legislatingBodyCode;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Convert(converter = YesNoConverter.class)
    @Default
    private boolean active = true;

}
