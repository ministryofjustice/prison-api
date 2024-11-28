package uk.gov.justice.hmpps.prison.repository.jpa.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SubAreaTest {

  @Test
  fun `should create SubArea with correct properties`() {
    // Given
    val code = "TEST"
    val description = "Test Sub Area"
    val active = true
    val areaType = AreaType.COMM
    val region = Region("REG", "Test Region")

    // When
    val subArea = SubArea(
      code = code,
      description = description,
      active = active,
      areaType = areaType,
      region = region,
    )

    // Then
    assertEquals(code, subArea.code)
    assertEquals(description, subArea.description)
    assertEquals(active, subArea.active)
    assertEquals(areaType, subArea.areaType)
    assertEquals(region, subArea.region)
    assertEquals("SUB_AREA", SubArea.TYPE)
  }

  @Test
  fun `should create SubArea with default active value`() {
    // Given
    val code = "TEST"
    val description = "Test Sub Area"
    val areaType = AreaType.COMM
    val region = Region("REG", "Test Region")

    // When
    val subArea = SubArea(
      code = code,
      description = description,
      areaType = areaType,
      region = region,
    )

    // Then
    assertTrue(subArea.active) // Default value should be true
  }

  @Test
  fun `should create inactive SubArea`() {
    // Given
    val code = "TEST"
    val description = "Test Sub Area"
    val areaType = AreaType.COMM
    val region = Region("REG", "Test Region")

    // When
    val subArea = SubArea(
      code = code,
      description = description,
      active = false,
      areaType = areaType,
      region = region,
    )

    // Then
    assertFalse(subArea.active)
  }
}
