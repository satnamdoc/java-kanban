package ru.yandex.practicum.taskmanagerapp.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;
import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class HistoryManagerTest<T extends HistoryManager> {
    protected T historyManager;
    protected static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    protected static final Duration TEST_DURATION = Duration.ofDays(1).plusHours(1).plusMinutes(1);
    protected static final Task task1 =
            new Task(100, "Test task #1", "description", TaskStatus.NEW, TEST_START_TIME, TEST_DURATION);
    protected static final Task task2 =
            new Task(101, "Test task #2", "description", TaskStatus.NEW, TEST_START_TIME, TEST_DURATION);
    protected static final Task task3 =
            new Task(102, "Test task #3", "description", TaskStatus.NEW, TEST_START_TIME, TEST_DURATION);

    @Test
    void shouldContainEmptyHistoryAfterInit() {
        assertTrue(historyManager.getHistory().isEmpty(),
                "Task history should be empty after history manager initilization");
    }

    /////////////
    /// add and remove 1, 2, 3 tasks to test internal linked list
    @Test
    void addOneTask() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        taskHistory.add(task1);
        historyManager.add(task1);
        assertEquals(taskHistory, historyManager.getHistory(), "Task history mismatch");
    }

    @Test
    void addTwoTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        taskHistory.add(task1);
        historyManager.add(task1);
        taskHistory.add(task2);
        historyManager.add(task2);
        assertEquals(taskHistory, historyManager.getHistory(), "Task history mismatch");
    }

    @Test
    void addThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        taskHistory.add(task1);
        historyManager.add(task1);
        taskHistory.add(task2);
        historyManager.add(task2);
        taskHistory.add(task3);
        historyManager.add(task3);
        assertEquals(taskHistory, historyManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeFirstOfThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(100);

        taskHistory.add(task2);
        taskHistory.add(task3);

        assertEquals(taskHistory, historyManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeSecondOfThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(101);

        taskHistory.add(task1);
        taskHistory.add(task3);

        assertEquals(taskHistory, historyManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeLastOfThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(102);

        taskHistory.add(task1);
        taskHistory.add(task2);

        assertEquals(taskHistory, historyManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeAllTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(100);
        historyManager.remove(101);

        assertTrue(historyManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    void shouldBeSingleRecordForTask()
    {
        ArrayList<Task> taskHistory = new ArrayList<>();

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        taskHistory.add(task2);
        taskHistory.add(task1);

        assertEquals(taskHistory, historyManager.getHistory(), "Task history mismatch");
    }
}
