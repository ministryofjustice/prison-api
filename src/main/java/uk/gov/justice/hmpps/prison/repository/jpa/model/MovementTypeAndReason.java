package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import org.hibernate.type.YesNoConverter;

import java.io.Serializable;

@Data
@Builder
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

    @Column(name = "esc_recap_flag")
    @Convert(converter = YesNoConverter.class)
    private boolean escaped = false;

    public static String getDescriptionOrNull(final MovementTypeAndReason referenceCode) {
        return referenceCode != null ? referenceCode.getDescription() : null;
    }
}
