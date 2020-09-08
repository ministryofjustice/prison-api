package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.parser.converter.SwaggerConverter;
import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerValidator extends ResourceTest {
    @Test
    public void test() {
        final var rootUri = testRestTemplate.getRootUri();
        final var result = new SwaggerConverter().readLocation(rootUri + "/api/swagger.json", null, null);
        assertThat(result.getMessages()).isEmpty();
    }
}
