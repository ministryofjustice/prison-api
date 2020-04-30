package net.syscon.elite.repository.jpa.model;

import lombok.*;
import net.syscon.elite.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import org.hibernate.annotations.ListIndexBase;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toUnmodifiableList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_PPTY_CONTAINERS")
public class OffenderPropertyContainer {

    @Id
    @Column(name = "PROPERTY_CONTAINER_ID")
    private Long containerId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERNAL_LOCATION_ID")
    private AgencyInternalLocation internalLocation;

    @Column(name = "ACTIVE_FLAG")
    private String activeFlag;

    @Column(name = "SEAL_MARK")
    private Long sealMark;

    public boolean isActive() {
        return activeFlag != null && activeFlag.equals("Y");
    }


}
