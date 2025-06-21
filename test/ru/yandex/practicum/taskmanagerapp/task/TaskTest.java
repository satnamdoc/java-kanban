package ru.yandex.practicum.taskmanagerapp.task;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.taskmanagerapp.task.Task.NULL_ID;

class TaskTest {
    private static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final Duration TEST_DURATION = Duration.ofDays(1).plusHours(1).plusMinutes(1);

    @Test
    public void toStringTest() {
        Task task = new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION);
        String taskStr = "Task{id=" + NULL_ID + ", name='Test task', status=NEW, description length=21, " +
                "start time=01.01.2025 00:00, duration=1d 1h 1m}";

        assertEquals(taskStr, task.toString(), "Task string view mismatch");
    }

    @Test
    public void getEndTime() {
        Task task = new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION);
        assertEquals(LocalDateTime.of(2025, 1, 2, 1, 1), task.getEndTime().get(), "Wrong end time");

        task = new Task("Test task", "Test task description", null, TEST_DURATION);
        assertTrue(task.getEndTime().isEmpty(), "Wrong end time");
    }
}