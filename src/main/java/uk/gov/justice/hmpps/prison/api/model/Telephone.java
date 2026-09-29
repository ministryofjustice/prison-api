package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Telephone Details
 **/
@Schema(description = "Telephone Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class Telephone {

    @Schema(description = "Phone Id", example = "2234232")
    private Long phoneId;

    @Schema(requiredMode = REQUIRED, description = "Telephone number", example = "0114 2345678")
    @NotBlank
    private String number;

    @Schema(requiredMode = REQUIRED, description = "Telephone type", example = "TEL")
    @NotBlank
    private String type;

    @Schema(description = "Telephone extension number", example = "123")
    private String ext;

    @Schema(description = "Date and time the telephone number was created", example = "2023-07-19T10:00:00")
    private LocalDateTime createDatetime;

    @Schema(description = "Date and time the telephone number was last modified", example = "2023-07-20T11:00:00")
    private LocalDateTime modifyDatetime;

}
