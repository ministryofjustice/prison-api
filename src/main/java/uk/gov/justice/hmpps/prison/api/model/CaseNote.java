package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Case Note
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Note")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString(of = {"caseNoteId", "bookingId", "type", "subType", "source", "authorName", "agencyId", "occurrenceDateTime"})
public class CaseNote {

    @ApiModelProperty(required = true, value = "Case Note Id (unique)", example = "12311312")
    @NotNull
    private Long caseNoteId;

    @ApiModelProperty(required = true, value = "Booking Id of offender", position = 1, example = "512321")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Case Note Type", position = 2, example = "KA")
    @NotBlank
    private String type;

    @ApiModelProperty(value = "Case Note Type Description", position = 3, example = "Key Worker Activity")
    private String typeDescription;

    @ApiModelProperty(required = true, value = "Case Note Sub Type", position = 4, example = "KS")
    @NotBlank
    private String subType;

    @ApiModelProperty(value = "Case Note Sub Type Description", position = 5, example = "Key Worker Session")
    private String subTypeDescription;

    @ApiModelProperty(required = true, value = "Source Type", position = 6, example = "INST")
    @NotBlank
    private String source;

    @ApiModelProperty(required = true, value = "Date and Time of Case Note creation", position = 7, example = "2017-10-31T01:30:00")
    @NotNull
    private LocalDateTime creationDateTime;

    @ApiModelProperty(required = true, value = "Date and Time of when case note contact with offender was made", position = 8, example = "2017-10-31T01:30:00")
    @NotNull
    private LocalDateTime occurrenceDateTime;

    @ApiModelProperty(required = true, value = "Id of staff member who created case note", position = 9, example = "321241")
    @NotNull
    private Long staffId;

    @ApiModelProperty(required = true, value = "Name of staff member who created case note (lastname, firstname)", position = 10, example = "Smith, John")
    @NotBlank
    private String authorName;

    @ApiModelProperty(required = true, value = "Case Note Text", position = 11, example = "This is some text")
    @NotBlank
    private String text;

    @ApiModelProperty(required = true, value = "The initial case note information that was entered", position = 12, example = "This is some text")
    @NotBlank
    private String originalNoteText;

    @ApiModelProperty(value = "Agency Code where Case Note was made.", position = 13, example = "MDI")
    private String agencyId;

    @ApiModelProperty(required = true, value = "Ordered list of amendments to the case note (oldest first)", position = 14)
    @NotNull
    @Builder.Default
    private List<CaseNoteAmendment> amendments = new ArrayList<>();

}
