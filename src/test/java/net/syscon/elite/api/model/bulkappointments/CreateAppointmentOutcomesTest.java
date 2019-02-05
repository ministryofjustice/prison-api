package net.syscon.elite.api.model.bulkappointments;

import net.syscon.elite.api.model.ScheduledEvent;
import org.junit.Test;

import static net.syscon.elite.api.model.bulkappointments.CreateAppointmentsOutcomes.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAppointmentOutcomesTest {

    @Test
    public void shouldCreateSuccessOutcome() {
        var outcomes = success(scheduledEvent());
        assertSizes(outcomes, 1, 0);
    }

    @Test
    public void shouldCreateFailureOutcome() {
        var outcomes = failure(rejectedAppointment());
        assertSizes(outcomes,0,1);
    }

    @Test
    public void accumulatorInitialisedEmpty() {
        var outcomes = accumulator();
        assertSizes(outcomes, 0,0);
    }

    @Test
    public void accumulation() {
        var acc = accumulator();
        acc.add(success(scheduledEvent()));
        assertSizes(acc,1,0);
        acc.add(failure(rejectedAppointment()));
        assertSizes(acc,1,1);
    }

    private static void assertSizes(CreateAppointmentsOutcomes outcomes, int successful, int failed) {
        assertThat(outcomes.getCreatedEvents()).hasSize(successful);
        assertThat(outcomes.getRejectedAppointments()).hasSize(failed);
    }

    private static ScheduledEvent scheduledEvent() {
        return ScheduledEvent.builder().build();
    }

    private static RejectedAppointment rejectedAppointment() {
        return RejectedAppointment.builder().build();
    }
}
