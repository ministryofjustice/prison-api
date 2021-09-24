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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"code", "statute"}, callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "OFFENCES")
@ToString
@IdClass(Offence.PK.class)
public class Offence {

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "HO_CODE")
    private HOCode hoCode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SEVERITY_RANKING")
    private String severityRanking;

    @Column(name = "ACTIVE_FLAG")
    @Type(type="yes_no")
    private boolean active;

    @Column(name = "LIST_SEQ")
    private Integer listSequence;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

}
