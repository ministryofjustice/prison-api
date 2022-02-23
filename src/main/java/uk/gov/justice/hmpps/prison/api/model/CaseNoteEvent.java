package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Case Note Event")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class CaseNoteEvent {
    @Schema(name = "noms_id", description = "Offender Noms Id", example = "A1417AE", required = true)
    private String nomsId;

    @Schema(required = true, description = "Case Note Id (unique)", example = "12311312")
    private Long id;

    @Schema(required = true, description = "Case Note Text", example = "This is some text")
    private String content;

    @Schema(required = true, description = "Date and Time of when case note contact with offender was made", example = "2017-10-31T01:30:00")
    private LocalDateTime contactTimestamp;

    @Schema(required = true, description = "Date and Time of notification of event", example = "2017-10-31T01:30:00")
    private LocalDateTime notificationTimestamp;

    @JsonIgnore
    private String firstName;
    @JsonIgnore
    private String lastName;

    @Schema(description = "Agency Code where Case Note was made.", example = "MDI")
    private String establishmentCode;

    @JsonIgnore
    private String mainNoteType;
    @JsonIgnore
    private String subNoteType;

    @Schema(required = true, description = "Case Note Type and Sub Type", example = "POS IEP_ENC")
    public String getNoteType() {
        return mainNoteType + " " + subNoteType;
    }

    @Schema(required = true, description = "Name of staff member who created case note (lastname, firstname)", example = "Smith, John")
    public String getStaffName() {
        return WordUtils.capitalizeFully(lastName + ", " + firstName);
    }
}
