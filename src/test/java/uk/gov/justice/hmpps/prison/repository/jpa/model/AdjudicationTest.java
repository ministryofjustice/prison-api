package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AdjudicationTest {

    @Test
    void getOffenderParty_returnsCorrectParty()
    {
        final var adjudication = Adjudication.builder()
            .build();

        final var offenderAdjudicationParty = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudication, 1L))
            .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
            .build();

        final var otherAdjudicationParty = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudication, 2L))
            .incidentRole("OTHER")
            .build();

        adjudication.setParties(List.of(offenderAdjudicationParty, otherAdjudicationParty));

        assertThat(adjudication.getOffenderParty().get().getId().getPartySeq()).isEqualTo(1L);
    }
}
