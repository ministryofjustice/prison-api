package uk.gov.justice.hmpps.prison.service.curfews;

import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class CurfewStateTest {
    @Test
    public void initialState() {
        assertThat(CurfewState.getState(
                HomeDetentionCurfew.
                        builder()
                        .build()))
                .isInstanceOf(InitialState.class);
    }

    @Test
    public void checksFailedState() {
        assertThat(CurfewState.getState(
                HomeDetentionCurfew.
                        builder()
                        .passed(Boolean.FALSE)
                        .checksPassedDate(LocalDate.now())
                        .build()
        )).isInstanceOf(ChecksFailedState.class);
    }

    @Test
    public void checksPassedState() {
        assertThat(CurfewState.getState(
                HomeDetentionCurfew.
                        builder()
                        .passed(Boolean.TRUE)
                        .checksPassedDate(LocalDate.now())
                        .build()
        )).isInstanceOf(ChecksPassedState.class);
    }

    @Test
    public void checksFailedRefusedState() {
        assertThat(CurfewState.getState(
                HomeDetentionCurfew.
                        builder()
                        .passed(Boolean.FALSE)
                        .checksPassedDate(LocalDate.now())
                        .approvalStatus("REJECTED")
                        .refusedReason("BREACH")
                        .approvalStatusDate(LocalDate.now())
                        .build()
        )).isInstanceOf(ChecksFailedRefusedState.class);
    }

    @Test
    public void checksPassedRefusedState() {
        assertThat(CurfewState.getState(
                HomeDetentionCurfew.
                        builder()
                        .passed(Boolean.TRUE)
                        .checksPassedDate(LocalDate.now())
                        .approvalStatus("REJECTED")
                        .refusedReason("BREACH")
                        .approvalStatusDate(LocalDate.now())
                        .build()
        )).isInstanceOf(ChecksPassedRefusedState.class);
    }

    @Test
    public void checksPassedApprovedState() {
        assertThat(CurfewState.getState(
                HomeDetentionCurfew.
                        builder()
                        .passed(Boolean.TRUE)
                        .checksPassedDate(LocalDate.now())
                        .approvalStatus("APPROVED")
                        .approvalStatusDate(LocalDate.now())
                        .build()
        )).isInstanceOf(ChecksPassedApprovedState.class);
    }
}
