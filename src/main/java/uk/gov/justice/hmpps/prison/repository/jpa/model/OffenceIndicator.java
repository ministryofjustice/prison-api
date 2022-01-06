package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"offence", "indicatorCode"}, callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "OFFENCE_INDICATORS")
@ToString
@IdClass(OffenceIndicator.PK.class)
public class OffenceIndicator {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private Offence offence;
        private String indicatorCode;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="OFFENCE_CODE", referencedColumnName="OFFENCE_CODE"),
        @JoinColumn(name="STATUTE_CODE", referencedColumnName="STATUTE_CODE")
    })
    private Offence offence;

    @Id
    @Column(name = "INDICATOR_CODE", nullable = false)
    private String indicatorCode;

}
