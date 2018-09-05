package net.syscon.elite.service.support;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class AlphaNumericComparatorTest {

    @Test
    public void ShouldHandleJustLetters() {
        List<String> words = Lists.newArrayList("ZZ", "BB", "AA");
        List<String> expected = Lists.newArrayList(Lists.newArrayList("AA", "BB", "ZZ"));

        Collections.sort(words, new AlphaNumericComparator());

        Assert.assertEquals(expected.toArray()[0], words.toArray()[0]);
        Assert.assertEquals(expected.toArray()[1], words.toArray()[1]);
        Assert.assertEquals(expected.toArray()[2], words.toArray()[2]);

    }

    @Test
    public void ShouldHandleJustNumbers() {
        List<String> words = Lists.newArrayList("33", "22", "11");
        List<String> expected = Lists.newArrayList(Lists.newArrayList("11", "22", "33"));

        Collections.sort(words, new AlphaNumericComparator());

        Assert.assertEquals(expected.toArray()[0], words.toArray()[0]);
        Assert.assertEquals(expected.toArray()[1], words.toArray()[1]);
        Assert.assertEquals(expected.toArray()[2], words.toArray()[2]);
    }

    @Test
    public void ShouldHandleWordsEndingWithNumbers() {
        List<String> words = Lists.newArrayList("work shop 10", "work shop 12", "work shop 1", "work");
        List<String> expected = Lists.newArrayList(Lists.newArrayList("work","work shop 1", "work shop 10", "work shop 12"));

        Collections.sort(words, new AlphaNumericComparator());

        compareLists(words, expected);
    }

    @Test
    public void ShouldHandleWordsStartingWithNumbers() {
        List<String> words = Lists.newArrayList("work shop 10", "work shop 2","WORK SHOP 3", "5-a-side", "aa");
        List<String> expected = Lists.newArrayList(Lists.newArrayList("5-a-side", "aa", "work shop 2", "WORK SHOP 3", "work shop 10"));

        Collections.sort(words, new AlphaNumericComparator());

        compareLists(words, expected);
    }

    @Test
    public void ShouldHandleNullWords() {
        List<String> words = Lists.newArrayList("work shop 10", "work shop 2",null,"");
        List<String> expected = Lists.newArrayList(Lists.newArrayList(null,"","work shop 2", "work shop 10"));

        Collections.sort(words, new AlphaNumericComparator());

        compareLists(words, expected);
    }

    @Test
    public void ShouldWorkWithMixedSet() {
        List<String> words = Lists.newArrayList("WORKSHOP 10", "WORKSHOP 2", "A", "bd2", "1test", "WORKSHOP 11", "WORKSHOP 0", "WORKSHOP 55", "1XS244R");
        List<String> expected = Lists.newArrayList(Lists.newArrayList("1test","1XS244R", "A","bd2","WORKSHOP 0", "WORKSHOP 2", "WORKSHOP 10", "WORKSHOP 11", "WORKSHOP 55"));

        Collections.sort(words, new AlphaNumericComparator());

        compareLists(words, expected);
    }

    @Test
    public void ShouldWorkWithDoubleDigits() {
        List<String> words = Lists.newArrayList( "W 11", "W 2", "W 09", "W 3");
        List<String> expected = Lists.newArrayList(Lists.newArrayList("W 2", "W 3", "W 09", "W 11"));

        Collections.sort(words, new AlphaNumericComparator());

        compareLists(words, expected);
    }

    private void compareLists(List<String> words, List<String> expected) {
        IntStream.range(0, words.size())
                .forEach(index -> Assert.assertEquals(expected.toArray()[index], words.toArray()[index]));
    }
}
