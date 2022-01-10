package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;

import javax.persistence.*;

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
