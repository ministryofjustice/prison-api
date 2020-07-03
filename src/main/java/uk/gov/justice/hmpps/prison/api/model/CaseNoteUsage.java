package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Case Note Type Usage
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Note Type Usage")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteUsage {
    @ApiModelProperty(required = true, value = "Offender No", example = "ZWE12A")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Case Note Type", position = 1, example = "KA")
    @NotBlank
    private String caseNoteType;

    @ApiModelProperty(required = true, value = "Case Note Sub Type", position = 2, example = "KS")
    @NotBlank
    private String caseNoteSubType;

    @ApiModelProperty(required = true, value = "Number of case notes of this type/subtype", position = 3, example = "5")
    @NotNull
    private Integer numCaseNotes;

    @ApiModelProperty(required = true, value = "Last case note of this type", position = 4, example = "2018-12-01T14:55:23")
    @NotNull
    private LocalDateTime latestCaseNote;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Map<String, Object> additionalProperties;


}
