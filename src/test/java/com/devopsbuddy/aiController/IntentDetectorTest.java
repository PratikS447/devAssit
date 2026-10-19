package com.devopsbuddy.aiController;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntentDetectorTest {
    private final IntentDetector detector = new IntentDetector();

    @Test
    void detectsCommandsWithoutDependingOnDefaultLocale() {
        assertEquals(IntentType.COMMAND, detector.detect("Show Docker containers"));
    }

    @Test
    void detectsQuestions() {
        assertEquals(IntentType.QUESTION, detector.detect("Why is this service failing?"));
    }

    @Test
    void handlesBlankInput() {
        assertEquals(IntentType.UNKNOWN, detector.detect("  "));
        assertEquals(IntentType.UNKNOWN, detector.detect(null));
    }
}
