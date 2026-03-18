package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles("test")
@JsonTest
public class AppointmentsToCreateJsonTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void deserializeJson() throws JacksonException {
        final var appointmentsToCreate = AppointmentsToCreate.builder().repeat(Repeat.builder().repeatPeriod(RepeatPeriod.WEEKLY).count(1).build()).build();
        final var serialized = jsonMapper.writeValueAsBytes(appointmentsToCreate);
        jsonMapper.readValue(serialized, AppointmentsToCreate.class);
    }
}
