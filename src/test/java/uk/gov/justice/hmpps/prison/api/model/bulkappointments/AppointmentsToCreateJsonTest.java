package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@ActiveProfiles("test")
@SpringBootTest
public class AppointmentsToCreateJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void deserializeJson() throws IOException {
        final var appointmentsToCreate = AppointmentsToCreate.builder().repeat(Repeat.builder().repeatPeriod(RepeatPeriod.WEEKLY).count(1).build()).build();
        final var serialized = objectMapper.writeValueAsBytes(appointmentsToCreate);
        objectMapper.readValue(serialized, AppointmentsToCreate.class);
    }
}
