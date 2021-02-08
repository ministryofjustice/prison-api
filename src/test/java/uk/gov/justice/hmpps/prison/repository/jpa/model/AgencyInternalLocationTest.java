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
                .activeFlag(ActiveFlag.Y)
                .certifiedFlag(ActiveFlag.N)
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

        assertThat(location.isActiveCellWithSpace()).isEqualTo(false);
    }

    @Test
    public void testHasSpace_NotFull(){
        final var location = AgencyInternalLocation.builder()
                .activeFlag(ActiveFlag.Y)
                .locationType("CELL")
                .capacity(100)
                .currentOccupancy(50)
                .build();

        assertThat(location.isActiveCellWithSpace()).isEqualTo(true);
    }


    @Test
    public void testHasSpace_Full(){
        final var location = AgencyInternalLocation.builder()
                .activeFlag(ActiveFlag.Y)
                .locationType("CELL")
                .capacity(100)
                .currentOccupancy(100)
                .build();

        assertThat(location.isActiveCellWithSpace()).isEqualTo(false);
    }
}
