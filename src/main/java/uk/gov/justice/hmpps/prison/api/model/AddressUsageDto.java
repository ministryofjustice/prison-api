package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "An Offender's address usage")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressUsageDto {
    @Schema(description = "Address ID of the associated address", example = "23422313")
    private Long addressId;
    @Schema(description = "The address usages", example = "HDC")
    private String addressUsage;
    @Schema(description = "The address usages description", example = "HDC Address")
    private String addressUsageDescription;
    @Schema(description = "Active Flag", example = "true")
    private Boolean activeFlag;
}
