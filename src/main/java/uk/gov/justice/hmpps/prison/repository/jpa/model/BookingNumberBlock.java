package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "BOOKING_NUMBER_BLOCKS")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BookingNumberBlock extends AuditableEntity {

    @Id
    @Column(name = "USE_SEQ", nullable = false)
    private Integer useSeq;

    @Column(name = "BOOKING_NUMBER_CHAR", nullable = false)
    private String firstChar;

    @Column(name = "BOOKING_NUMBER_START", nullable = false)
    private Integer start;

    @Column(name = "BOOKING_NUMBER_END", nullable = false)
    private Integer end;

    @Column(name = "LAST_USED")
    private Integer lastUsed;

    @Column(name = "ACTIVE_FLAG")
    @Type(type="yes_no")
    private boolean active;

    @Column(name = "USED_FLAG", nullable = false)
    @Type(type="yes_no")
    private boolean usedFlag;

    @Column(name = "PREFIX_OR_SUFFIX", nullable = false)
    @Default
    private String prefixOrSuffix = "P";


}
