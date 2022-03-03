package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@ApiModel(description = "Hold Response")
@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonInclude(Include.NON_NULL)
public class Hold {

    @ApiModelProperty(value = "Hold Number", name = "hold_number", example = "6185835", position = 1)
    @JsonProperty("hold_number")
    private Long holdNumber;

    @ApiModelProperty(value = "Client unique reference", name = "client_unique_ref", example = "jerkincrocus", position = 2)
    @JsonProperty("client_unique_ref")
    private String clientUniqueRef;

    @ApiModelProperty(value = "Reference number", name = "reference_no", example = "TEST0075", position = 3)
    @JsonProperty("reference_no")
    private String referenceNo;

    @ApiModelProperty(value = "Description", name = "description", example = "Hold via API", position = 4)
    private String description;

    @ApiModelProperty(value = "Entry date", name = "entry_date", example = "2017-06-23", position = 5)
    @JsonProperty("entry_date")
    private LocalDate entryDate;

    @ApiModelProperty(value = "Amount in pence", name = "amount", example = "150", position = 6)
    private Long amount;

    @ApiModelProperty(value = "Hold until date", name = "hold_until_date", example = "2017-07-07", position = 7)
    @JsonProperty("hold_until_date")
    private LocalDate holdUntilDate;

}
