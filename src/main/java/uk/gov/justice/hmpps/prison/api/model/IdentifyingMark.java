package uk.gov.justice.hmpps.prison.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@SuppressWarnings("unused")
@Schema(description = "Identifying Mark")
@JsonInclude(Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class IdentifyingMark {

    @Schema(description = "The id of the identifying mark", requiredMode = REQUIRED)
    @NotNull
    private Long id;

    @Schema(description = "Offender Unique Reference", example = "A1234AA", requiredMode = REQUIRED)
    @NotBlank
    private String offenderNo;

    @Schema(description = "The body part the mark is on", requiredMode = REQUIRED)
    private String bodyPart;

    @Schema(description = "The type of distinguishing mark (e.g. tattoo, scar)", requiredMode = REQUIRED)
    private String markType;

    @Schema(description = "The side of the body part the mark is on", requiredMode = NOT_REQUIRED)
    private String side;

    @Schema(description = "The orientation of the mark on the body part (e.g. Centre, Low, Upper)", requiredMode = NOT_REQUIRED)
    private String partOrientation;

    @Schema(description = "Comment about the distinguishing mark", requiredMode = NOT_REQUIRED)
    private String comment;

    @Schema(description = "The date and time the data was last modified", requiredMode = REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "Username of the user that last modified this field", example = "USER1", requiredMode = REQUIRED)
    private String createdBy;

}
