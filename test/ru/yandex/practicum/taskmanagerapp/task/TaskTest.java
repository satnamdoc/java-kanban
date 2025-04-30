package ru.yandex.practicum.taskmanagerapp.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.taskmanagerapp.task.Task.NULL_ID;

class TaskTest {
    @Test
    public void toStringTest() {
        Task task = new Task("Test task", "Test task description");
        String taskStr = "Task{id=" + NULL_ID + ", name='Test task', status=NEW, description length=21}";

        assertEquals(taskStr, task.toString(), "Task string view mismatch");
    }
}