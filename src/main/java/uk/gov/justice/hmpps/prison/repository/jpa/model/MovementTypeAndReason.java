package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "MOVEMENT_REASONS")
@IdClass(MovementTypeAndReason.Pk.class)
public class MovementTypeAndReason implements Serializable {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    @Builder
    public static class Pk implements Serializable {
        @Column(name = "MOVEMENT_TYPE", updatable = false, insertable = false)
        private String type;
        @Column(name = "MOVEMENT_REASON_CODE", updatable = false, insertable = false)
        private String reasonCode;
    }

    @Id
    private String type;

    @Id
    private String reasonCode;

    private String description;

    public static String getDescriptionOrNull(final MovementTypeAndReason referenceCode) {
        return referenceCode != null ? referenceCode.getDescription() : null;
    }
}
