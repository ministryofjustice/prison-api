package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"offence", "indicatorCode"}, callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "OFFENCE_INDICATORS")
@ToString(exclude = {"offence"})
public class OffenceIndicator {

    @Id
    @Column(name = "OFFENCE_INDICATOR_ID", nullable = false)
    private Long offenceIndicatorId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="OFFENCE_CODE", referencedColumnName="OFFENCE_CODE"),
        @JoinColumn(name="STATUTE_CODE", referencedColumnName="STATUTE_CODE")
    })
    private Offence offence;

    @Column(name = "INDICATOR_CODE", nullable = false)
    private String indicatorCode;

}
