package uk.gov.justice.hmpps.prison.health;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PingEndpointTest {
    @Test
    public void ping() {
        assertThat(new PingEndpoint().ping()).isEqualTo("pong");
    }
}
