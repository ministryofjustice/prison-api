package uk.gov.justice.hmpps.prison.api.model.v1

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Transaction Response")
data class Transaction(
  @Schema(description = "ID of created transaction", example = "6179604-1")
  val id: String,
)
