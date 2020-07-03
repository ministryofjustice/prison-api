package net.syscon.prison.api.resource;

import io.swagger.v3.parser.converter.SwaggerConverter;
import net.syscon.prison.api.resource.impl.ResourceTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerValidator extends ResourceTest {
    @Test
    public void test() {
        final var rootUri = testRestTemplate.getRootUri();
        final var result = new SwaggerConverter().readLocation(rootUri + "/api/swagger.json", null, null);
        assertThat(result.getMessages()).isEmpty();
    }
}
