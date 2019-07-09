package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"personal_details", "sentence_information", "location", "warnings", "entitlement", "case_details"})
public class PssDetailData {

    @ApiModelProperty(value = "Personal details", name = "personal_details", position = 0)
    @JsonProperty("personal_details")
    private PssPersonalData personalData;

    @ApiModelProperty(value = "Sentence information", name = "sentence_information", position = 1)
    @JsonProperty("sentence_information")
    private PssSentenceData sentenceData;

    @ApiModelProperty(value = "Location information", name = "location", position = 2)
    @JsonProperty("location")
    private PssLocationData locationData;

    @ApiModelProperty(value = "Warnings", name = "warnings", position = 3)
    @JsonProperty("warnings")
    private List<PssWarningItem> warningData;

    @ApiModelProperty(value = "Entitlements", name = "entitlement", position = 4)
    @JsonProperty("entitlement")
    private PssEntitlementData entitlementData;

    @ApiModelProperty(value = "Case details", name = "case_details", position = 5)
    @JsonProperty("case_details")
    private PssCaseDetailsData caseDetailData;
}
