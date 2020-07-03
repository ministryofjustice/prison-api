package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.text.WordUtils;

import java.time.LocalDateTime;


@Getter
@ApiModel(description = "Case Note Event")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class CaseNoteEvent {
    @ApiModelProperty(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true, position = 1)
    private String nomsId;

    @ApiModelProperty(required = true, value = "Case Note Id (unique)", example = "12311312", position = 2)
    private Long id;

    @ApiModelProperty(required = true, value = "Case Note Text", position = 3, example = "This is some text")
    private String content;

    @ApiModelProperty(required = true, value = "Date and Time of when case note contact with offender was made", position = 4, example = "2017-10-31T01:30:00")
    private LocalDateTime contactTimestamp;

    @ApiModelProperty(required = true, value = "Date and Time of notification of event", position = 5, example = "2017-10-31T01:30:00")
    private LocalDateTime notificationTimestamp;

    @JsonIgnore
    private String firstName;
    @JsonIgnore
    private String lastName;

    @ApiModelProperty(value = "Agency Code where Case Note was made.", position = 7, example = "MDI")
    private String establishmentCode;

    @JsonIgnore
    private String mainNoteType;
    @JsonIgnore
    private String subNoteType;

    @ApiModelProperty(required = true, value = "Case Note Type and Sub Type", position = 8, example = "POS IEP_ENC")
    public String getNoteType() {
        return mainNoteType + " " + subNoteType;
    }

    @ApiModelProperty(required = true, value = "Name of staff member who created case note (lastname, firstname)", position = 6, example = "Smith, John")
    public String getStaffName() {
        return WordUtils.capitalizeFully(lastName + ", " + firstName);
    }
}
