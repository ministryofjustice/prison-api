package uk.gov.justice.hmpps.prison.service.support;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SortAssertion {
    private final List<String> actual;


    SortAssertion(final String... input) {
        this.actual = Arrays.asList(input);
    }

    void sortsTo(final String... expected) {
        actual.sort(new AlphaNumericComparator());

        assertThat(actual)
            .isEqualTo(Arrays.asList(expected));
    }
}

public class AlphaNumericComparatorTest {

    @Test
    public void ShouldHandleJustLetters() {
        expectThat(
            "ZZ",
            "BB",
            "AA"
        ).sortsTo(
            "AA",
            "BB",
            "ZZ"
        );
    }

    @Test
    public void ShouldHandleJustNumbers() {
        expectThat(
            "33",
            "22",
            "11"
        ).sortsTo(
            "11",
            "22",
            "33"
        );
    }

    @Test
    public void ShouldHandleWordsEndingWithNumbers() {
        expectThat(
            "work shop 10",
            "work shop 12",
            "work shop 1",
            "work"
        ).sortsTo(
            "work",
            "work shop 1",
            "work shop 10",
            "work shop 12"
        );
    }

    @Test
    public void ShouldHandleWordsStartingWithNumbers() {
        expectThat(
            "work shop 10",
            "work shop 2",
            "WORK SHOP 3",
            "5-a-side",
            "aa"
        ).sortsTo(
            "5-a-side",
            "aa",
            "work shop 2",
            "WORK SHOP 3",
            "work shop 10"
        );
    }

    @Test
    public void ShouldHandleNullWords() {
        expectThat(
            "work shop 10",
            "work shop 2",
            null,
            ""
        ).sortsTo(
            null,
            "",
            "work shop 2",
            "work shop 10"
        );
    }

    @Test
    public void ShouldWorkWithMixedSet() {
        expectThat(
            "WORKSHOP 10",
            "WORKSHOP 2",
            "A",
            "bd2",
            "1test",
            "WORKSHOP 11",
            "WORKSHOP 0",
            "WORKSHOP 55",
            "1XS244R"
        ).sortsTo(
            "1test",
            "1XS244R",
            "A",
            "bd2",
            "WORKSHOP 0",
            "WORKSHOP 2",
            "WORKSHOP 10",
            "WORKSHOP 11",
            "WORKSHOP 55"
        );
    }

    @Test
    public void ShouldWorkWithDoubleDigits() {
        expectThat(
            "W 11",
            "W 2",
            "W 09",
            "W 3"
        ).sortsTo(
            "W 2",
            "W 3",
            "W 09",
            "W 11"
        );
    }

    @Test
    public void ShouldWorkWithLocationsThatEndWithSymbol() {
        expectThat("WORKSHOP 0",
            "WORKSHOP (HDC)",
            "WORKSHOP (HDC)"
        ).sortsTo(
            "WORKSHOP (HDC)",
            "WORKSHOP (HDC)",
            "WORKSHOP 0"
        );
    }

    private static SortAssertion expectThat(final String... input) {
        return new SortAssertion(input);
    }
}
