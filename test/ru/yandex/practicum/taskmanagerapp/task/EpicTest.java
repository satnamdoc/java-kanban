package ru.yandex.practicum.taskmanagerapp.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.taskmanagerapp.task.Task.NULL_ID;

class EpicTest {
    private static Epic epic;
    private static final int TEST_SUBTASK_ID = 0x10000000;

    @BeforeEach
    public void beforeEach() {
        epic = new Epic("Test epic", "Test epic description");
        epic.addSubtask(TEST_SUBTASK_ID);
    }

    @Test
    void shouldContainsTestIdSubtask() {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(1, subtaskIds.size(), "Test epic should have the only one subtask");
        assertEquals(TEST_SUBTASK_ID, subtaskIds.getFirst(),
                "Test epic should have subtask with TEST_SUBTASK_ID id");
    }

    @Test
    void removeSubtask() {
        epic.removeSubtask(TEST_SUBTASK_ID);
        assertTrue(epic.getSubtaskIds().isEmpty(), "Test epic shouldn't have subtasks");
    }

    @Test
    void clearSubtasks() {
        epic.clearSubtasks();
        assertTrue(epic.getSubtaskIds().isEmpty(), "Test epic shouldn't have subtasks");
    }

    @Test
    public void toStringTest() {
        String epicStr = "Epic{id=" + NULL_ID + ", name='Test epic', status=NEW, description length=21, " +
                "start time=UNKNOWN, duration=0d 0h 0m, " +
                "subtaskIds=[" + TEST_SUBTASK_ID + "]}";
        assertEquals(epicStr, epic.toString(), "Epic string view mismatch");
    }

    @Test
    public void getEndTime() {
        assertTrue(epic.getEndTime().isEmpty(), "New epic should have no start time");
    }
}