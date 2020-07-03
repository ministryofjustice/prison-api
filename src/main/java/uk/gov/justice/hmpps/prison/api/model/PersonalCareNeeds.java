package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ApiModel(description = "Personal Care Needs")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PersonalCareNeeds {
    @ApiModelProperty(value = "Offender No")
    @JsonInclude(NON_NULL)
    String offenderNo;

    @ApiModelProperty(value = "Personal Care Needs")
    final List<PersonalCareNeed> personalCareNeeds;
}
