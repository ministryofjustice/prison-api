package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "Visit Restriction")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class VisitRestriction {

    @ApiModelProperty(value = "Type", name = "type", position = 1)
    @JsonProperty("type")
    private CodeDescription restrictionType;

    @ApiModelProperty(value = "Effective Date", name = "effective_date", example = "2019-01-01", position = 2)
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "Expiry Date", name = "expiry_date", example = "2019-01-01", position = 3)
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "Comment Text", name = "comment_text", position = 4)
    @JsonProperty("comment_text")
    private String commentText;


}
