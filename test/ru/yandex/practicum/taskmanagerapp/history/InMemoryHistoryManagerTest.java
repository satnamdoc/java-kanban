package ru.yandex.practicum.taskmanagerapp.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Task;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static InMemoryHistoryManager inMemoryHistoryManager;

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

        Task task = new Task("Test task", "description");
        task.setId(100);
        taskHistory.add(task);
        inMemoryHistoryManager.add(task);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void addTwoTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        Task task1 = new Task("Test task #1", "description");
        Task task2 = new Task("Test task #2", "description");
        task1.setId(100);
        taskHistory.add(task1);
        inMemoryHistoryManager.add(task1);
        task2.setId(101);
        taskHistory.add(task2);
        inMemoryHistoryManager.add(task2);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void addThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        Task task1 = new Task("Test task #1", "description");
        Task task2 = new Task("Test task #2", "description");
        Task task3 = new Task("Test task #3", "description");
        task1.setId(100);
        taskHistory.add(task1);
        inMemoryHistoryManager.add(task1);
        task2.setId(101);
        taskHistory.add(task2);
        inMemoryHistoryManager.add(task2);
        task3.setId(102);
        taskHistory.add(task3);
        inMemoryHistoryManager.add(task3);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void removeFirstOfThreeTasks() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        Task task1 = new Task("Test task #1", "description");
        Task task2 = new Task("Test task #2", "description");
        Task task3 = new Task("Test task #3", "description");

        task1.setId(100);
        task2.setId(101);
        task3.setId(102);

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

        Task task1 = new Task("Test task #1", "description");
        Task task2 = new Task("Test task #2", "description");
        Task task3 = new Task("Test task #3", "description");

        task1.setId(100);
        task2.setId(101);
        task3.setId(102);

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

        Task task1 = new Task("Test task #1", "description");
        Task task2 = new Task("Test task #2", "description");
        Task task3 = new Task("Test task #3", "description");

        task1.setId(100);
        task2.setId(101);
        task3.setId(102);

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
        Task task1 = new Task("Test task #1", "description");
        Task task2 = new Task("Test task #2", "description");

        task1.setId(100);
        task2.setId(101);

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

        Task task1 = new Task("Test task #1", "description");
        Task task2 = new Task("Test task #2", "description");

        task1.setId(100);
        task2.setId(101);

        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task1);

        taskHistory.add(task2);
        taskHistory.add(task1);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }
}