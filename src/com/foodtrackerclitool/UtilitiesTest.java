package com.foodtrackerclitool;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.LinkPermission;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UtilitiesTest {

    @ParameterizedTest(name="Run {index}, inputString={0}, expectedResult={1}")
    @MethodSource("testQuitPromptParameters")
    void testQuitPrompt(String input, boolean expectedOutput) {
        //Arrange
        //Act
        boolean result = Utilities.QuitPrompt(input);

        //Assert
        Assert.assertEquals("Incorrect return value", result, expectedOutput);
    }

    @ParameterizedTest(name="Run {index}: inputString={0}, expectedResult={1}")
    @MethodSource("testToTitleCaseParameters")
    void testToTitleCase(String input, String expectedOutput) {
        //Arrange
        //Act
        String result = Utilities.ToTitleCase(input);

        //Assert
        Assert.assertEquals("String not converted correctly", result, expectedOutput);
    }

    @ParameterizedTest(name="Run {index}: input={0}, expectedResult={1}")
    @MethodSource("testQuitPromptParameters")
    void renameCorruptedFile() {
    }

    static Stream<Arguments> testToTitleCaseParameters() throws Throwable {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("s", "S"),
                Arguments.of("S", "S"),
                Arguments.of("string", "String"),
                Arguments.of("test string", "Test String"),
                Arguments.of("test String", "Test String"),
                Arguments.of("tEST sTRING", "Test String")
        );
    }

    static Stream<Arguments> testQuitPromptParameters() throws Throwable {
        return Stream.of(
                Arguments.of("", false),
                Arguments.of("t", false),
                Arguments.of("q", true),
                Arguments.of("Q", true),
                Arguments.of("quit", true),
                Arguments.of("Quit", true),
                Arguments.of("Quiit", false)
        );
    }

    static Stream<Arguments> testRenameCorruptFileParameters() throws Throwable {
        return Stream.of(
                Arguments.of("", false),
                Arguments.of("t", false),
                Arguments.of("q", true),
                Arguments.of("Q", true),
                Arguments.of("quit", true),
                Arguments.of("Quit", true),
                Arguments.of("Quiit", false)
        );
    }

    @ParameterizedTest(name="Run {index}: input={0}, expectedResult={1}")
    @MethodSource("testIsPyramidWord")
    void isPyramidWord(String input, boolean expected) {
        boolean result = Utilities.isPyramidWord(input);

        Assert.assertEquals("wrong result", expected, result);
    }

    static Stream<Arguments> testIsPyramidWord() throws Throwable {
        return Stream.of(
                Arguments.of("", false),
                Arguments.of("t", true),
                Arguments.of("q", true),
                Arguments.of("Q", true),
                Arguments.of("quit", false),
                Arguments.of("banana", true),
                Arguments.of("bandana", false),
                Arguments.of("bandaa", false),
                Arguments.of("abarberaba", true),
                Arguments.of("a b a r b eraba", false),
                Arguments.of("abarb eraba", false)
        );
    }

    @ParameterizedTest(name="Run {index}: input={0}, expectedResult={1}")
    @MethodSource("testIsConsecutiveSum")
    void isConsecutiveSum(int input, int expected) {
        int n = Utilities.isConsecutiveSum(input);

        Assert.assertEquals("not correct return", expected, n);
    }

    static Stream<Arguments> testIsConsecutiveSum() throws Throwable {
        return Stream.of(
                Arguments.of(1, 1),
                Arguments.of(2, -1),
                Arguments.of(3, 2),
                Arguments.of(4, -1),
                Arguments.of(5, -1),
                Arguments.of(6, 3)
        );
    }
}