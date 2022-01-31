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

    @Test
    void getOffenderParty_returnsCorrectAdjudicationParties() {
        final var adjudication = Adjudication.builder()
            .build();
        final var offenderAdjudicationParty = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudication, 1L))
            .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
            .build();
        final var victimStaffParty =
            AdjudicationParty.builder()
                .id(new AdjudicationParty.PK(adjudication, 2L))
                .incidentRole(Adjudication.INCIDENT_ROLE_VICTIM)
                .staff(Staff.builder().build())
                .build();
        final var victimOffenderParty =
            AdjudicationParty.builder()
                .id(new AdjudicationParty.PK(adjudication, 3L))
                .incidentRole(Adjudication.INCIDENT_ROLE_VICTIM)
                .offenderBooking(OffenderBooking.builder().build())
                .build();
        final var connectedOffenderParty =
            AdjudicationParty.builder()
                .id(new AdjudicationParty.PK(adjudication, 4L))
                .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
                .offenderBooking(OffenderBooking.builder().build())
                .build();
        adjudication.getParties().addAll(List.of(offenderAdjudicationParty, victimStaffParty, victimOffenderParty, connectedOffenderParty));
        assertThat(adjudication.getConnectedOffenderParties()).containsOnly(connectedOffenderParty);
        assertThat(adjudication.getVictimsStaffParties()).containsOnly(victimStaffParty);
        assertThat(adjudication.getVictimsOffenderParties()).containsOnly(victimOffenderParty);
    }

    @Test
    public void getOffenderParty_returnsCorrectMaxSequence(){
        final var adjudication = Adjudication.builder()
            .build();
        final var offenderAdjudicationParty = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudication, 1L))
            .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
            .build();
        final var victimStaffParty =
            AdjudicationParty.builder()
                .id(new AdjudicationParty.PK(adjudication, 10L))
                .incidentRole(Adjudication.INCIDENT_ROLE_VICTIM)
                .staff(Staff.builder().build())
                .build();
        adjudication.getParties().addAll(List.of(offenderAdjudicationParty, victimStaffParty));
        assertThat(adjudication.getMaxSequence()).isEqualTo(10l);
    }
}
