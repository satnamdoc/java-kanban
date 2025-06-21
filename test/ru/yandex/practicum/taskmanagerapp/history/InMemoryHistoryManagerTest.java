package ru.yandex.practicum.taskmanagerapp.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static InMemoryHistoryManager inMemoryHistoryManager;
    private static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final Duration TEST_DURATION = Duration.ofDays(1).plusHours(1).plusMinutes(1);
    private static final Task task1 =
            new Task(100, "Test task #1", "description", TaskStatus.NEW, TEST_START_TIME, TEST_DURATION);
    private static final Task task2 =
            new Task(101, "Test task #2", "description", TaskStatus.NEW, TEST_START_TIME, TEST_DURATION);
    private static final Task task3 =
            new Task(102, "Test task #3", "description", TaskStatus.NEW, TEST_START_TIME, TEST_DURATION);

    @BeforeEach
    public void beforeEach() {
        inMemoryHistoryManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldContainEmptyHistoryAfterInit() {
        assertTrue(inMemoryHistoryManager.getHistory().isEmpty(),
                "Task history should be empty after history manager initilization");
    }

    /////////////
    /// add and remove 1, 2, 3 tasks to test internal linked list
    @Test
    void addOneTask() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        taskHistory.add(task1);
        inMemoryHistoryManager.add(task1);
        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void addTwoTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        taskHistory.add(task1);
        inMemoryHistoryManager.add(task1);
        taskHistory.add(task2);
        inMemoryHistoryManager.add(task2);
        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void addThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        taskHistory.add(task1);
        inMemoryHistoryManager.add(task1);
        taskHistory.add(task2);
        inMemoryHistoryManager.add(task2);
        taskHistory.add(task3);
        inMemoryHistoryManager.add(task3);
        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeFirstOfThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.remove(100);

        taskHistory.add(task2);
        taskHistory.add(task3);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeSecondOfThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.remove(101);

        taskHistory.add(task1);
        taskHistory.add(task3);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeLastOfThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.remove(102);

        taskHistory.add(task1);
        taskHistory.add(task2);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeAllTasks() {
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.remove(100);
        inMemoryHistoryManager.remove(101);

        assertTrue(inMemoryHistoryManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    void shouldBeSingleRecordForTask()
    {
        ArrayList<Task> taskHistory = new ArrayList<>();

        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task1);

        taskHistory.add(task2);
        taskHistory.add(task1);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }
}