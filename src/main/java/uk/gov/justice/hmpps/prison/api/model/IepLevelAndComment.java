package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IepLevelAndComment {

    @Schema(description = "The IEP level. Must be one of the reference data values in domain 'IEP_LEVEL'. Typically BAS (Basic), ENH (Enhanced), ENT (Entry) or STD (Standard)", required = true, example = "BAS")
    @NotBlank(message = "The IEP level must not be blank")
    private String iepLevel;


    @Schema(description = "A comment (reason for change) for the new IEP Level", required = true, example = "The submitting officer's reasons for changing the IEP level. Should be clear and concise.")
    @NotBlank(message = "The IEP comment must not be blank")
    @Size(min = 1, max = 240, message = "The IEP level must have comment text of between 1 and 240 characters")
    private String comment;

    @Schema(description = "Time review occurred, default is now")
    private LocalDateTime reviewTime;

    @Schema(description = "The reviewer user name - will take the calling user name if not supplied")
    private String reviewerUserName;
}
