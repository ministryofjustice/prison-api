package uk.gov.justice.hmpps.prison.util;

import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalUtilTest {

    @Test
    void getOrNullReturnsValueIfNotNull() {

        final var id = "abc";
        final var instance = DummyClass
            .builder()
            .clazz(DummyClass.builder().id(id).build())
            .build();

        assertThat(OptionalUtil.getOrNull(instance.getClazz(), DummyClass::getId)).isEqualTo(id);
    }

    @Test
    void getOrNullReturnsNullIfReferenceIsNull() {

        final var instance = DummyClass
            .builder()
            .clazz(null)
            .build();

        assertThat(OptionalUtil.getOrNull(instance.getClazz(), DummyClass::getId)).isNull();
    }


    @Getter
    @Builder
    private static class DummyClass {
        private final DummyClass clazz;
        private final String id;
    }
}