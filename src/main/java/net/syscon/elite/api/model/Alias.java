package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Alias
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Alias")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Alias {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotNull
    private Integer age;

    @NotNull
    private LocalDate dob;

    @NotBlank
    private String gender;

    private String ethnicity;

    @NotBlank
    private String nameType;

    @NotNull
    private LocalDate createDate;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * First name of offender alias
      */
    @ApiModelProperty(required = true, value = "First name of offender alias")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
      * Middle names of offender alias
      */
    @ApiModelProperty(value = "Middle names of offender alias")
    @JsonProperty("middleName")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    /**
      * Last name of offender alias
      */
    @ApiModelProperty(required = true, value = "Last name of offender alias")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
      * Age of Offender
      */
    @ApiModelProperty(required = true, value = "Age of Offender")
    @JsonProperty("age")
    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    /**
      * Date of Birth of Offender
      */
    @ApiModelProperty(required = true, value = "Date of Birth of Offender")
    @JsonProperty("dob")
    public LocalDate getDob() {
        return dob;
    }

    public void setDob(final LocalDate dob) {
        this.dob = dob;
    }

    /**
      * Gender
      */
    @ApiModelProperty(required = true, value = "Gender")
    @JsonProperty("gender")
    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    /**
      * Ethnicity
      */
    @ApiModelProperty(value = "Ethnicity")
    @JsonProperty("ethnicity")
    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(final String ethnicity) {
        this.ethnicity = ethnicity;
    }

    /**
      * Type of Alias
      */
    @ApiModelProperty(required = true, value = "Type of Alias")
    @JsonProperty("nameType")
    public String getNameType() {
        return nameType;
    }

    public void setNameType(final String nameType) {
        this.nameType = nameType;
    }

    /**
      * Date of creation
      */
    @ApiModelProperty(required = true, value = "Date of creation")
    @JsonProperty("createDate")
    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(final LocalDate createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class Alias {\n");
        
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  middleName: ").append(middleName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  age: ").append(age).append("\n");
        sb.append("  dob: ").append(dob).append("\n");
        sb.append("  gender: ").append(gender).append("\n");
        sb.append("  ethnicity: ").append(ethnicity).append("\n");
        sb.append("  nameType: ").append(nameType).append("\n");
        sb.append("  createDate: ").append(createDate).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
