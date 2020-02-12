package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "REFERENCE_CODES")
@DiscriminatorColumn(name = "domain")
@Inheritance
public abstract class ReferenceCode implements Serializable {
    @Id
    private String domain;
    @Id
    private String code;
    private String description;

    public static final String LEG_CASE_TYP = "LEG_CASE_TYP";
    public static final String MLTY_BRANCH = "MLTY_BRANCH";
    public static final String MLTY_WZONE = "MLTY_WZONE";
    public static final String MLTY_DSCHRG = "MLTY_DSCHRG";
    public static final String MLTY_DISCP = "MLTY_DISCP";
    public static final String MLTY_RANK = "MLTY_RANK";

    public static String getDescriptionOrNull(final ReferenceCode referenceCode) {
        return referenceCode != null ? referenceCode.getDescription() : null;
    }

    public static String getCodeOrNull(final ReferenceCode referenceCode) {
        return referenceCode != null ? referenceCode.getCode() : null;
    }
}
