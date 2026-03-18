package uk.gov.justice.hmpps.prison.executablespecification

import io.cucumber.java.DefaultDataTableCellTransformer
import io.cucumber.java.DefaultDataTableEntryTransformer
import io.cucumber.java.DefaultParameterTransformer
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import java.lang.reflect.Type

class ParameterTypes {
  private val jsonMapper = JsonMapper.builder()
    .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    .build()

  @DefaultParameterTransformer
  @DefaultDataTableEntryTransformer
  @DefaultDataTableCellTransformer
  fun transformer(fromValue: Any?, toValueType: Type?): Any? = jsonMapper.convertValue(fromValue, jsonMapper.constructType(toValueType))
}
