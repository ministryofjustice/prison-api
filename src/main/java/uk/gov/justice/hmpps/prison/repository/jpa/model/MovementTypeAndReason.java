package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.type.YesNoConverter;

import java.io.Serializable;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TYPE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MOVEMENT_REASONS")
public class MovementTypeAndReason implements Serializable {

    public MovementTypeAndReason(MovementType movementType, String reasonCode, String description) {
        this(new Pk(movementType.getCode(), reasonCode), movementType, description, false);
    }

    @NoArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    @Builder
    @Embeddable
    public static class Pk implements Serializable {
        @Column(name = "MOVEMENT_TYPE", updatable = false, insertable = false)
        private String type;

        @Column(name = "MOVEMENT_REASON_CODE", updatable = false, insertable = false)
        private String reasonCode;

        public Pk(MovementType movementType, String reasonCode) {
            this(movementType.getCode(), reasonCode);
        }

        public Pk(String type, String reasonCode) {
            this();
            this.type = type;
            this.reasonCode = reasonCode;
        }
    }

    public String getReasonCode() {
        return id.getReasonCode();
    }
    public String getCode() {
        return id.getReasonCode();
    }

    @EmbeddedId
    private Pk id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "MOVEMENT_TYPE", referencedColumnName = "code", insertable = false, updatable = false))
    })
    private MovementType movementType;

    private String description;

    @Column(name = "esc_recap_flag")
    @Convert(converter = YesNoConverter.class)
    @Builder.Default
    private boolean escaped = false;
}
