package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Agency Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Agency {
    @Schema(required = true, description = "Agency identifier.", example = "MDI")
    private String agencyId;

    @Schema(required = true, description = "Agency description.", example = "Moorland (HMP & YOI)")
    private String description;

    @Schema(description = "Long description of the agency", example = "Moorland (HMP & YOI)")
    private String longDescription;

    @Schema(required = true, description = "Agency type.  Reference domain is AGY_LOC_TYPE", example = "INST", allowableValues = "CRC,POLSTN,INST,COMM,APPR,CRT,POLICE,IMDC,TRN,OUT,YOT,SCH,STC,HOST,AIRPORT,HSHOSP,HOSPITAL,PECS,PAR,PNP,PSY")
    private String agencyType;

    @Schema(required = true, description = "Indicates the Agency is active", example = "true")
    @Default
    private boolean active = true;

    @Schema(description = "Court Type.  Reference domain is JURISDICTION", example = "CC", allowableValues = "CACD,CB,CC,CO,DCM,GCM,IMM,MC,OTHER,YC")
    private String courtType;

    @Schema(description = "Date agency became inactive", example = "2012-01-12")
    private LocalDate deactivationDate;

    @Schema(description = "List of addresses associated with agency")
    private List<AddressDto> addresses;

    @Schema(description = "List of phones associated with agency")
    private List<Telephone> phones;

    @Schema(description = "List of emails associated with agency")
    private List<Email> emails;
}
