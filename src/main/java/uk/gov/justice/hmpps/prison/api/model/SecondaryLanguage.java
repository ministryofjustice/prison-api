package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Secondary language")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class SecondaryLanguage {
    @Schema(description = "Booking id", example = "10000")
    private Long bookingId;
    @Schema(description = "Language code", example = "ENG")
    private String code;
    @Schema(description = "Language description", example = "English")
    private String description;
    @Schema(description = "Reading proficiency")
    private Boolean canRead;
    @Schema(description = "Writing proficiency")
    private Boolean canWrite;
    @Schema(description = "Speaking proficiency")
    private Boolean canSpeak;
}
