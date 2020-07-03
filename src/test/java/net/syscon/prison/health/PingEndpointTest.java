package net.syscon.prison.health;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PingEndpointTest {
    @Test
    public void ping() {
        assertThat(new PingEndpoint().ping()).isEqualTo("pong");
    }
}
