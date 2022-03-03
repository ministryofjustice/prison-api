package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "Legal Case")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"case_info_number", "case_active", "case_started", "court", "legal_case_type", "charges"})
@ToString
@Builder
public class LegalCase {

    @JsonIgnore
    @ApiModelProperty(value = "Case Id", position = 0, example = "2323133")
    private Long caseId;

    @ApiModelProperty(value = "Case Information Number", position = 1, example = "1254232")
    @JsonProperty("case_info_number")
    private String caseInfoNumber;

    @ApiModelProperty(value = "Case Active", position = 2, example = "true")
    @JsonProperty("case_active")
    private boolean caseActive;

    @ApiModelProperty(value = "Case Started Date", position = 3, example = "2019-01-17")
    @JsonProperty("case_started")
    private LocalDate beginDate;

    @ApiModelProperty(value = "Court", position = 4, example = "{ \"code\": \"ABDRCT\", \"desc\": \"Aberdare County Court\" }")
    private CodeDescription court;

    @ApiModelProperty(value = "Legal Case Type", position = 5, example = "{ \"code\": \"A\", \"desc\": \"Adult\" }")
    @JsonProperty("legal_case_type")
    private CodeDescription caseType;

    @ApiModelProperty(value = "Charges", position = 6)
    List<Charge> charges;

}
