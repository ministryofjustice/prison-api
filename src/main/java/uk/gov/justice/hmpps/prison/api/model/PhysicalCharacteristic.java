package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * Physical Characteristic
 **/
@SuppressWarnings("unused")
@Schema(description = "Physical Characteristic")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class PhysicalCharacteristic {
    @NotBlank
    private String type;

    @NotBlank
    private String characteristic;

    @NotBlank
    private String detail;

    private Long imageId;

    public PhysicalCharacteristic(@NotBlank String type, @NotBlank String characteristic, @NotBlank String detail, Long imageId) {
        this.type = type;
        this.characteristic = characteristic;
        this.detail = detail;
        this.imageId = imageId;
    }

    public PhysicalCharacteristic() {
    }

    /**
     * Type code of physical characteristic
     */
    @Schema(required = true, description = "Type code of physical characteristic")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Type of physical characteristic
     */
    @Schema(required = true, description = "Type of physical characteristic")
    @JsonProperty("characteristic")
    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(final String characteristic) {
        this.characteristic = characteristic;
    }

    /**
     * Detailed information about the physical characteristic
     */
    @Schema(required = true, description = "Detailed information about the physical characteristic")
    @JsonProperty("detail")
    public String getDetail() {
        return detail;
    }

    public void setDetail(final String detail) {
        this.detail = detail;
    }

    /**
     * Image Id Ref
     */
    @Schema(description = "Image Id Ref")
    @JsonProperty("imageId")
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(final Long imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class PhysicalCharacteristic {\n");

        sb.append("  type: ").append(type).append("\n");
        sb.append("  characteristic: ").append(characteristic).append("\n");
        sb.append("  detail: ").append(detail).append("\n");
        sb.append("  imageId: ").append(imageId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
