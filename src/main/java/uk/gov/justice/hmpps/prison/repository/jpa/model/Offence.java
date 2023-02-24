package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"code", "statute"}, callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "OFFENCES")
@ToString
@IdClass(Offence.PK.class)
@NamedEntityGraph(name = "offence-entity-graph",
    attributeNodes = {
        @NamedAttributeNode("statute"),
        @NamedAttributeNode("hoCode"),
    }
)
@With
public class Offence {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private String code;
        private String statute;
    }

    @Id
    @Column(name = "OFFENCE_CODE", nullable = false)
    private String code;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "STATUTE_CODE", nullable = false)
    private Statute statute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HO_CODE")
    private HOCode hoCode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SEVERITY_RANKING")
    private String severityRanking;

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    @Column(name = "LIST_SEQ")
    private Integer listSequence;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @OneToMany(mappedBy = "offence", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Default
    private List<OffenceIndicator> offenceIndicators = new ArrayList<>();

    public boolean isMoreSeriousThan(Offence other) {
        return isNumeric(this.getSeverityRanking()) && isNumeric(other.getSeverityRanking()) &&
            //Smaller number is more severe
            Long.parseLong(this.getSeverityRanking()) < Long.parseLong(other.getSeverityRanking());
    }
}
