package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@DiscriminatorValue(OffenderAddress.ADDR_TYPE)
@NoArgsConstructor
@Data
@EqualsAndHashCode(exclude = "offender", callSuper = true)
@ToString(of = {"offender"}, callSuper = true)
@NamedEntityGraph(
    name = "address",
    attributeNodes = {
        @NamedAttributeNode("city"),
        @NamedAttributeNode("county"),
        @NamedAttributeNode("country"),
        @NamedAttributeNode("addressType"),
        @NamedAttributeNode("phones"),
        @NamedAttributeNode(value = "addressUsages", subgraph = "address-usage-details"),
    },
    subgraphs = {
        @NamedSubgraph(
            name = "address-usage-details",
            attributeNodes = {
                @NamedAttributeNode("addressUsageType"),
            }
        ),
    }
)
public class OffenderAddress extends Address {

    static final String ADDR_TYPE = "OFF";

    @JoinColumn(name = "OWNER_ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Offender offender;
}
