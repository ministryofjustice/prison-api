package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@ActiveProfiles("test")

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppointmentsToCreateJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void deserializeJson() throws IOException {
        val appointmentsToCreate = AppointmentsToCreate.builder().repeat(Repeat.builder().repeatPeriod(RepeatPeriod.WEEKLY).count(1).build()).build();
        byte[] serialized = objectMapper.writeValueAsBytes(appointmentsToCreate);
        objectMapper.readValue(serialized, AppointmentsToCreate.class);
    }
}
