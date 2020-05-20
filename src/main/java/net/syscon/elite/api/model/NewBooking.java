package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * New Offender Booking
 **/
@SuppressWarnings("unused")
@ApiModel(description = "New Offender Booking")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class NewBooking {

    @ApiModelProperty(required = true, value = "The offender's last name.", example = "Mark")
    @Length(max = 35)
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "The offender's first name.", example = "John")
    @Length(max = 35)
    @NotBlank
    private String firstName;

    @ApiModelProperty(value = "The offender's middle name.", example = "Luke")
    @Length(max = 35)
    private String middleName1;

    @ApiModelProperty(value = "An additional middle name for the offender.", example = "Matthew")
    @Length(max = 35)
    private String middleName2;

    @ApiModelProperty(value = "A code representing the offender's title (from TITLE reference domain).", example = "MR", allowableValues = "BR,DAME,DR,FR,IMAM,LADY,LORD,MISS,MR,MRS,MS,RABBI,REV,SIR,SR")
    @Length(max = 12)
    private String title;

    @ApiModelProperty(value = "A code representing a suffix to apply to offender's name (from SUFFIX reference domain).", example = "JR", allowableValues = "I,II,III,IV,IX,V,VI,VII,VIII,JR,SR")
    @Length(max = 12)
    private String suffix;

    @ApiModelProperty(required = true, value = "The offender's date of birth. Must be specified in YYYY-MM-DD format. Current has to match YJAF allowed DOB", example = "1970-01-01")
    @NotNull
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "A code representing the offender's gender (from the SEX reference domain).", example = "M", allowableValues = "M,F,NK,NS,REF")
    @Length(max = 12)
    @NotBlank
    private String gender;

    @ApiModelProperty(required = true, value = "A code representing the reason for the offender's admission.", example = "N", allowableValues = "24,25,26,27,29,A,ADMN,B,C,CCOM,CLIF,CONR,CRT,D,DCYP,DHMP,E,ELR,ETB,F,FINE,FOREIGN,G,H,HLF,I,IMMIG,INT,INTER,J,JAIL,K,L,LICR,M,MED,N,O,P,PSUS,PSYC,Q,R,RDTO,RECA,RHDC,RMND,S,SENT,T,TRN,TRNCRT,TRNTAP,U,V,W,Y,YDET,Z")
    @NotBlank
    private String reason;

    @ApiModelProperty(value = "A flag to indicate that the offender is a youth/young offender (or not). Defaults to false if not specified.", example = "false")
    private boolean youthOffender;

    @ApiModelProperty(value = "A code representing the offender's ethnicity (from the ETHNICITY reference domain).", example = "W1", allowableValues = "A9,B1,B2,B9,M1,M2,M3,M9,NS,O1,O2,O9,W1,W2,W3,W8,W9")
    @Length(max = 12)
    private String ethnicity;

    @ApiModelProperty(value = "A unique offender number. If set, a new booking will be created for an existing offender. If not set, a new offender and new offender booking will be created (subject to de-duplication checks).", example = "A1234AA")
    @Length(max = 10)
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$")
    private String offenderNo;

    @ApiModelProperty(value = "The offender's PNC (Police National Computer) number.", example = "03/11999M")
    @Length(max = 20)
    private String pncNumber;

    @ApiModelProperty(value = "The offender's CRO (Criminal Records Office) number.")
    @Length(max = 20)
    private String croNumber;

    @ApiModelProperty(value = "An external system identifier for the offender or offender booking. This may be useful if the booking is being created by an external system.", example = "REF001")
    @Length(max = 20)
    private String externalIdentifier;

    @ApiModelProperty(value = "A code representing the type of external identifier specified in <i>externalIdentifier</> property (from ID_TYPE reference domain).", example = "YJAF", allowableValues = "YJAF,CRO,DL,EXTERNAL_REL,HMPS,HOREF,LIDS,MERGED,MERGE_HMPS,NINO,NOMS,NPD,PASS,PNC,SPNC,STAFF")
    @Length(max = 12)
    private String externalIdentifierType;

    @ApiModelProperty(value = "A unique correlation id for idempotent request control.", example = "1000021100")
    @Length(max = 36)
    private String correlationId;

    @ApiModelProperty(value = "Prison ID (Agency ID) of where to place offender", example = "MDI")
    @Length(max = 3)
    private String prisonId;
}