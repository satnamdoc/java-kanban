package ru.yandex.practicum.taskmanagerapp.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.taskmanagerapp.task.Task.NULL_ID;

class SubTaskTest {
    private final static int TEST_EPIC_ID = 0x10000000;

    @Test
    public void toStringTest() {
        SubTask subTask = new SubTask("Test subtask", "Test subtask description", TEST_EPIC_ID);
        String subTaskStr = "SubTask{id=" + NULL_ID + ", name='Test subtask', status=NEW, description length=24, epicId="
                + TEST_EPIC_ID + "}";

        assertEquals(subTaskStr, subTask.toString(), "Subtask string view mismatch");
    }
}