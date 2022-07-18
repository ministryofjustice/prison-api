package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Request release of prisoner")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestToDischargePrisoner {

    @NotNull
    @Schema(description = "Agency Location code for hospital, agency type is HSHOSP", example = "HAZLWD")
    private String hospitalLocationCode;

    @Schema(required = true, description = "The time the release occurred, if not supplied it will be the current time. Note: Time can be in the past but not before the last movement", example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dischargeTime;

    @Schema(description = "Additional comments about the release", example = "Prisoner was released on bail")
    @Length(max = 240, message = "Comments size is a maximum of 240 characters")
    private String commentText;

    @Schema(description = "Supporting Prison for POM, can be null if prisoner is already in a prison, for prisoners already released this field will be ignored", example = "MDI")
    @Length(max = 3, message = "Prison ID is 3 character code")
    private String supportingPrisonId;

    @Schema(description = "Where the prisoner has moved from e.g. court, can be null if prisoner is already in prison, for prisoners already in prison this field will be ignored", example = "SHEFCC")
    @Length(max = 6, message = "From location")
    private String fromLocationId;
}
