package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AgencyInternalLocationTest {

    @Test
    public void testCellCswap_isFalse() {
        final var location = AgencyInternalLocation.builder().build();

        assertThat(location.isCellSwap()).isEqualTo(false);
    }

    @Test
    public void testCellCswap_isTrue() {
        final var location = AgencyInternalLocation
                .builder()
                .active(true)
                .certifiedFlag(false)
                .parentLocation(null)
                .locationCode("CSWAP")
                .build();

        assertThat(location.isCellSwap()).isEqualTo(true);
    }

    @Test
    public void testHasSpace_NotActiveNotCell(){
        final var location = AgencyInternalLocation.builder()
                .capacity(100)
                .currentOccupancy(50)
                .build();

        assertThat(location.isActiveCellWithSpace(false)).isEqualTo(false);
    }

    @Test
    public void testHasSpace_IgnoreZeroOperationalCapacity(){
        final var location = AgencyInternalLocation.builder()
                .active(true)
                .locationType("CELL")
                .operationalCapacity(0)
                .capacity(10)
                .currentOccupancy(5)
                .build();

        assertThat(location.isActiveCellWithSpace(true)).isEqualTo(true);
    }

    @Test
    public void testHasSpace_UseZeroOperationalCapacity(){
        final var location = AgencyInternalLocation.builder()
            .active(true)
            .locationType("CELL")
            .operationalCapacity(0)
            .capacity(10)
            .currentOccupancy(5)
            .build();

        assertThat(location.isActiveCellWithSpace(false)).isEqualTo(false);
    }

    @Test
    public void testHasSpace_NotFull(){
        final var location = AgencyInternalLocation.builder()
                .active(true)
                .locationType("CELL")
                .capacity(100)
                .currentOccupancy(50)
                .build();

        assertThat(location.isActiveCellWithSpace(false)).isEqualTo(true);
    }


    @Test
    public void testHasSpace_Full(){
        final var location = AgencyInternalLocation.builder()
                .active(true)
                .locationType("CELL")
                .capacity(100)
                .currentOccupancy(100)
                .build();

        assertThat(location.isActiveCellWithSpace(false)).isEqualTo(false);
    }

    @Test
    public void testCapacity_IgnoreZeroOperationalCapacity(){
        final var location = AgencyInternalLocation.builder()
            .active(true)
            .locationType("CELL")
            .operationalCapacity(0)
            .capacity(10)
            .currentOccupancy(5)
            .build();

        assertThat(location.getActualCapacity(true)).isEqualTo(10);
    }

    @Test
    public void testCapacity_UseZeroOperationalCapacity(){
        final var location = AgencyInternalLocation.builder()
            .active(true)
            .locationType("CELL")
            .operationalCapacity(0)
            .capacity(10)
            .currentOccupancy(5)
            .build();

        assertThat(location.getActualCapacity(false)).isEqualTo(0);
    }
}
