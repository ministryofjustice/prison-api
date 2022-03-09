package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Schema(description = "Military Records")
@Data
@AllArgsConstructor
public class MilitaryRecords {
    @Schema(description = "Military Records")
    final List<MilitaryRecord> militaryRecords;
}
