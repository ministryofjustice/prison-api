package net.syscon.elite.service.transformers;

import net.syscon.elite.api.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.CourtEvent.CourtEventBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CourtEventTransformerTest {

    private final CourtEventBuilder builder = net.syscon.elite.repository.jpa.model.CourtEvent.builder();

    @Disabled("Need to fill in the other attributes e.g. from and to agency on the offender booking.")
    @Test
    void transform() {
        var transformed = CourtEventTransformer.transform(builder
                .id(-1L)
                .commentText("Lorem Ipsum")
                .directionCode("OUT")
                .build());

        assertThat(transformed).isEqualTo(CourtEvent.builder()
                .eventId(-1L)
                .commentText("Lorem Ipsum")
                .directionCode("OUT")
                .build());
    }
}
