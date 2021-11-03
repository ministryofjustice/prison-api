package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "Update Offender Dates Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestToUpdateOffenderDates {

    @ApiModelProperty(required = true, value = "Dates to be updated for the offender's sentences.")
    private List<SentenceDates> sentenceDates;

    @ApiModelProperty(required = true, value = "Key dates to be updated for the offender.")
    private OffenderKeyDates keyDates;
}
