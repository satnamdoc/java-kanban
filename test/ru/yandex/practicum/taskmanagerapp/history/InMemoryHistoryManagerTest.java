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

    @Test
    void add() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        Task task = new Task("Test task", "description");
        task.setId(100);
        taskHistory.add(task);
        inMemoryHistoryManager.add(task);

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

    @Test
    void addMaximumTasksToHistory() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        for (int i = 0; i < InMemoryHistoryManager.HISTORY_SIZE; i++) {
            Task task = new Task("Test task", "description");
            task.setId(i + 100);
            taskHistory.add(task);
            inMemoryHistoryManager.add(task);
        }

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(),"Task history mismatch");
    }

    @Test
    void addOverMaximumTasksToHistory() {
        ArrayList<Task> taskHistory = new ArrayList<>();

        for (int i = 0; i < InMemoryHistoryManager.HISTORY_SIZE + 1; i++) {
            Task task = new Task("Test task", "description");
            task.setId(i + 100);
            taskHistory.add(task);
            inMemoryHistoryManager.add(task);
        }
        taskHistory.removeFirst();

        assertEquals(taskHistory, inMemoryHistoryManager.getHistory(), "Task history mismatch");
    }

}