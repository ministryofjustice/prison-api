package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@ApiModel(description = "Secondary language")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class SecondaryLanguage {
    @ApiModelProperty(value = "Booking id", example = "10000")
    private Long bookingId;
    @ApiModelProperty(value = "Language code", example = "ENG")
    private String code;
    @ApiModelProperty(value = "Language description", example = "English")
    private String description;
    @ApiModelProperty(value = "Reading proficiency")
    private Boolean canRead;
    @ApiModelProperty(value = "Writing proficiency")
    private Boolean canWrite;
    @ApiModelProperty(value = "Speaking proficiency")
    private Boolean canSpeak;
}
