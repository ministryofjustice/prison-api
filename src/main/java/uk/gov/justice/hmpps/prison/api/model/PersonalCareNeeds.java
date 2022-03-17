package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Schema(description = "Personal Care Needs")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PersonalCareNeeds {
    @Schema(description = "Offender No")
    @JsonInclude(NON_NULL)
    String offenderNo;

    @Schema(description = "Personal Care Needs")
    final List<PersonalCareNeed> personalCareNeeds;
}
