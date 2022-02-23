package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OIC_OFFENCES")
@ToString(of = {"offenceId"})
public class AdjudicationOffenceType extends AuditableEntity {

    @Id
    @Column(name = "OIC_OFFENCE_ID")
    private Long offenceId;

    @Column(name = "OIC_OFFENCE_CODE", nullable = false)
    private String offenceCode;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;
}
