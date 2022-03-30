package uk.gov.justice.hmpps.prison.api.model

data class PhysicalCharacteristicDto(
  val type: String?,
  val characteristic: String?,
  val detail: String?,
  val imageId: Long?,
) {
  fun toPhysicalCharacteristic() = PhysicalCharacteristic(
    this.type,
    this.characteristic,
    this.detail,
    this.imageId,
  )
}
