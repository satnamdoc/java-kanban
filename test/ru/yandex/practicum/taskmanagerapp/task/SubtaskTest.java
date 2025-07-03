package ru.yandex.practicum.taskmanagerapp.task;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.taskmanagerapp.task.Task.NULL_ID;

class SubtaskTest {
    private final static int TEST_EPIC_ID = 0x10000000;
    private static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final Duration TEST_DURATION = Duration.ofDays(1).plusHours(1).plusMinutes(1);


    @Test
    public void toStringTest() {
        Subtask subtask = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, TEST_EPIC_ID);
        String subtaskStr = "Subtask{id=" + NULL_ID + ", name='Test subtask', status=NEW, " +
                "description length=24, start time=01.01.2025 00:00, duration=1d 1h 1m, " +
                "epicId=" + TEST_EPIC_ID + "}";

        assertEquals(subtaskStr, subtask.toString(), "Subtask string view mismatch");
    }
}