package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"code", "statute"}, callSuper = false)
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
        private String code;
        private Statute statute;
    }

    @Id
    @Column(name = "OFFENCE_CODE", nullable = false)
    private String code;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "STATUTE_CODE", nullable = false)
    private Statute statute;

    @Column(name = "OFFENCE_CODE", nullable = false)
    private String indicatorCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="OFFENCE_CODE", referencedColumnName="OFFENCE_CODE"),
        @JoinColumn(name="STATUTE_CODE", referencedColumnName="STATUTE_CODE")
    })
    private Offence offence;
}
