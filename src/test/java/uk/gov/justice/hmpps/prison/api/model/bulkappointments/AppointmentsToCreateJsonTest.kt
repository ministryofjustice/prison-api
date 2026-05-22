package uk.gov.justice.hmpps.prison.api.model.bulkappointments

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

@ActiveProfiles("test")
@JsonTest
class AppointmentsToCreateJsonTest(@Autowired private val jsonMapper: JsonMapper) {

  @Test
  @Throws(JacksonException::class)
  fun deserializeJson() {
    val appointmentsToCreate =
      AppointmentsToCreate.builder().repeat(Repeat.builder().repeatPeriod(RepeatPeriod.WEEKLY).count(1).build()).build()
    val serialized = jsonMapper.writeValueAsBytes(appointmentsToCreate)
    jsonMapper.readValue(serialized, AppointmentsToCreate::class.java)
  }
}
