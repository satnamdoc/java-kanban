package ru.yandex.practicum.taskmanagerapp.taskmanager;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.exception.*;
import ru.yandex.practicum.taskmanagerapp.exception.NullPointerException;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.taskmanagerapp.task.Task.NULL_ID;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected static final int TEST_ID = 0x10000000;
    protected static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    protected static final Duration TEST_DURATION = Duration.ofDays(1);

    @Test
    void addTask() {
        Task task = new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION);
        taskManager.addTask(task);

        List<Task> tasks = taskManager.getTaskList();

        assertEquals(1, tasks.size(), "Wrong number of tasks");
        assertEquals(task, tasks.getFirst(), "Tasks are not equal");
    }

    @Test
    void addEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        taskManager.addEpic(epic);

        List<Epic> epics = taskManager.getEpicList();

        assertEquals(1, epics.size(), "Wrong number of epics");
        assertEquals(epic, epics.getFirst(), "Epics are not equal");
    }

    @Test
    void addSubtask() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        taskManager.addSubtask(subtask);

        List<Subtask> subtasks = taskManager.getSubtaskList();

        assertEquals(1, subtasks.size(), "Wrong number of subtasks");
        assertEquals(subtask, subtasks.getFirst(), "Subtasks are not equal");
    }

    @Test
    public void shouldNotAddNullTaskObject() {
        assertThrows(NullPointerException.class, () -> taskManager.addTask(null),
                "Should throw exception for null task");
        assertTrue(taskManager.getTaskList().isEmpty(), "Task list should be empty");
    }

    @Test
    public void shouldNotAddNullEpicObject() {
        assertThrows(NullPointerException.class, () -> taskManager.addEpic(null),
                "Should throw exception for null epic");
        assertTrue(taskManager.getEpicList().isEmpty(), "Epic list should be empty");
    }

    @Test
    public void shouldNotAddEpicContainingSubtasks() {
        Epic epic = new Epic("Test epic", "Test epic description");
        epic.addSubtask(TEST_ID);
        assertThrows(InconsistentDataException.class, () -> taskManager.addEpic(epic),
                "Should throw exception when epic has subtasks");
        assertTrue(taskManager.getEpicList().isEmpty(), "Epic list should be empty");
    }

    @Test
    public void shouldNotAddNullSubtaskObject() {
        assertThrows(NullPointerException.class, () -> taskManager.addSubtask(null),
                "Should throw exception for null subtask");
        assertTrue(taskManager.getSubtaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    public void shouldNotAddSubtaskForNonExistingEpic() {
        assertThrows(InconsistentDataException.class,
                () -> taskManager.addSubtask(
                    new Subtask("Test subtask", "Test subtask description", TEST_START_TIME,
                    TEST_DURATION, TEST_ID)),
                "Should throw exception for nonexisten epic");
        assertTrue(taskManager.getSubtaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    public void shouldNotUpdateNullTaskObject() {
        assertThrows(NullPointerException.class, () -> taskManager.updateTask(null),
                "Should throw exception for null task");
    }

    @Test
    public void shouldNotUpdateNonExistingTask() {
        assertThrows(NotFoundException.class,
                () -> taskManager.updateTask(
                        new Task(TEST_ID, "Updated", "Updated", TaskStatus.NEW,
                                TEST_START_TIME, TEST_DURATION)),
                "Should throw exception for nonexisting task");
    }

    @Test
    void updateTask() {
        Task task = new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION);
        int taskId = taskManager.addTask(task);

        Task updatedTask = new Task("Updated", "Updated", TEST_START_TIME, TEST_DURATION);
        updatedTask.setId(taskId);

        assertEquals(updatedTask, taskManager.updateTask(updatedTask), "Tasks are not equal");
    }


    @Test
    public void shouldNotUpdateNullEpicObject() {
        assertThrows(NullPointerException.class, () -> taskManager.updateEpic(null),
                "Should throw exception for null epic");
    }

    @Test
    public void shouldNotUpdateNonExistingEpic() {
        Epic epic = new Epic("Updated", "Updated");
        epic.setId(TEST_ID);
        assertThrows(NotFoundException.class, () -> taskManager.updateEpic(epic),
                "Should throw exception for nonexisting epic");
    }

    @Test
    public void updateEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Epic updatedEpic = new Epic("Updated", "Updated");
        updatedEpic.setId(epicId);
        assertEquals(updatedEpic, taskManager.updateEpic(updatedEpic), "Epics are not equal");
    }

    @Test
    public void shouldNotUpdateNullSubtaskObject() {
        assertThrows(NullPointerException.class, () -> taskManager.updateSubtask(null),
                "Should throw exception for null subtask");
    }

    @Test
    public void shouldNotUpdateNonExistingSubtask() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        Subtask subtask = new Subtask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        subtask.setId(TEST_ID);
        assertThrows(NotFoundException.class, () -> taskManager.updateSubtask(subtask),
                "Should throw exception for nonexisting subtask");
    }

    @Test
    public void updateSubtask() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        Subtask subtask = new Subtask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        int subtaskId = taskManager.addSubtask(subtask);

        Subtask updatedSubtask = new Subtask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        updatedSubtask.setId(subtaskId);

        assertEquals(updatedSubtask, taskManager.updateSubtask(updatedSubtask),
                "Subtasks are not equal");
    }


    @Test
    void getEpicSubtasks() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        taskManager.addSubtask(subtask);

        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);

        assertEquals(1, subtasks.size(), "Wrong number of subtasks");
        assertEquals(subtask, subtasks.getFirst(), "Subtasks are not equal");
    }

    @Test
    void clear() {
        taskManager.addTask(new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION));
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        taskManager.addSubtask(new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plus(TEST_DURATION), TEST_DURATION, epicId));

        taskManager.clear();

        assertTrue(taskManager.getTaskList().isEmpty(), "Task list should be empty");
        assertTrue(taskManager.getEpicList().isEmpty(), "Epic list should be empty");
        assertTrue(taskManager.getSubtaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void clearTasks() {
        taskManager.addTask(new Task("Test task", "Test task description",
                TEST_START_TIME, TEST_DURATION));
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        taskManager.addSubtask(new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plus(TEST_DURATION), TEST_DURATION, epicId));

        taskManager.clearTasks();

        assertTrue(taskManager.getTaskList().isEmpty(), "Task list should be empty");
    }

    @Test
    void shouldRemoveAllTasksFromEpicListAndSubtaskListAfterEpicListClearing() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        taskManager.addSubtask(new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.clearEpics();

        assertTrue(taskManager.getEpicList().isEmpty(), "Epic list should be empty");
        assertTrue(taskManager.getSubtaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void clearSubtasks() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        taskManager.addSubtask(new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.clearSubtasks();

        assertTrue(taskManager.getSubtaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void shouldNotRemoveNonExistingTask() {
        assertThrows(NotFoundException.class, () -> taskManager.removeTask(TEST_ID),
                "removeTask should throw exception for nonexisten task");
    }

    @Test
    void removeTask() {
        Task task = new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION);
        int taskId = taskManager.addTask(task);
        assertEquals(task, taskManager.removeTask(taskId), "Should return removed task");
        assertThrows(NotFoundException.class, () -> taskManager.getTask(taskId),
                "Removed task is still in the manager");
    }

    @Test
    void shouldNotRemoveNonExistingEpic() {
        assertThrows(NotFoundException.class, () -> taskManager.removeEpic(TEST_ID),
                "removeEpic should throw exception for nonexisten epic");
    }

    @Test
    void removeEpic() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask",
                "Test subtask description", TEST_START_TIME, TEST_DURATION, epicId));
        assertEquals(epicId, taskManager.removeEpic(epicId).getId(), "Should return removed epic");
        assertThrows(NotFoundException.class, () -> taskManager.getEpic(epicId),
                "Removed epic is still in the manager");
        assertThrows(NotFoundException.class, () -> taskManager.getSubtask(subtaskId),
                "Subtask of the removed epic is still in the manager");
    }

    @Test
    void shouldNotRemoveNonExistingSubtask() {
        assertThrows(NotFoundException.class, () -> taskManager.removeSubtask(TEST_ID),
                "removeSubtask should throw exception for nonexisten subtask");
    }

    @Test
    void removeSubtask() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask",
                "Test subtask description", TEST_START_TIME, TEST_DURATION, epicId));
        assertEquals(subtaskId, taskManager.removeSubtask(subtaskId).getId(), "Should return removed subtask");
        assertThrows(NotFoundException.class, () -> taskManager.getSubtask(subtaskId),
                "Removed subtask is still in the manager");
    }

    /// ////////////////////////
    /// Epic status management tests
    @Test
    public void shouldSetEpicStatusToNewWhenAllSubtasksAreNew() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(2), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask3);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }

    @Test
    public void shouldSetEpicStatusToNewWhenAllSubtasksAreDone() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(2), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask3);

        assertEquals(TaskStatus.DONE,  taskManager.getEpic(epicId).getStatus(), "Status is not DONE");
    }

    @Test
    public void shouldSetEpicStatusToInProgressWhenSubtasksAreNewAndDone() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.NEW,
                TEST_START_TIME.plusDays(2), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask3);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(), "Status is not IN_PROGRESS");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenOneSubtaskIsInProgress() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.IN_PROGRESS,
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.NEW,
                TEST_START_TIME.plusDays(2), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask3);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(), "Status is not IN PROGRESS");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubtasksRemoval() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);

        taskManager.updateSubtask(new Subtask(subtask1.getId(), "Test subtask", "Test subtask description",
                TaskStatus.IN_PROGRESS, TEST_START_TIME, Duration.ofDays(1), epicId));

        taskManager.removeSubtask(subtask2.getId());
        taskManager.removeSubtask(subtask1.getId());

        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus(), "Status is not NEW");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubtaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);

        taskManager.updateSubtask(new Subtask(subtask1.getId(), "Test subtask", "Test subtask description",
                TaskStatus.IN_PROGRESS, TEST_START_TIME, Duration.ofDays(1), epicId));
        taskManager.updateSubtask(new Subtask(subtask1.getId(), "Test subtask", "Test subtask description",
                TaskStatus.NEW, TEST_START_TIME, Duration.ofDays(1), epicId));

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }


    @Test
    public void shouldSetEpicStatusToInProgressAfterSubtaskAdding() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.updateSubtask(new Subtask(subtask1.getId(), "Test subtask", "Test subtask description",
                TaskStatus.DONE, TEST_START_TIME, Duration.ofDays(1), epicId));
        Subtask subtask2 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(), "Status is not IN_PRPGRESS");
    }

    @Test
    public void shouldSetEpicStatusToInProgressAfterSubtaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);

        taskManager.updateSubtask(new Subtask(subtask1.getId(), "Test subtask", "Test subtask description",
                TaskStatus.IN_PROGRESS, TEST_START_TIME, Duration.ofDays(1), epicId));

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(), "Status is not IN_PRPGRESS");
    }

    @Test
    public void shouldSetEpicStatusToDoneAfterSubtaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);

        taskManager.updateSubtask(new Subtask(subtask1.getId(), "Test subtask", "Test subtask description",
                TaskStatus.DONE, TEST_START_TIME, Duration.ofDays(1), epicId));
        taskManager.updateSubtask(new Subtask(subtask2.getId(), "Test subtask", "Test subtask description",
                TaskStatus.DONE, TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId));

        assertEquals(TaskStatus.DONE, taskManager.getEpic(epicId).getStatus(), "Status is not DONE");
    }

    @Test
    public void shouldSetEpicStatusToDoneAfterSubtaskRemoval() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubtask(subtask2);

        taskManager.updateSubtask(new Subtask(subtask1.getId(), "Test subtask", "Test subtask description",
                TaskStatus.DONE, TEST_START_TIME, Duration.ofDays(1), epicId));
        taskManager.removeSubtask(subtask2.getId());

        assertEquals(TaskStatus.DONE,  taskManager.getEpic(epicId).getStatus(), "Status is not DONE");
    }

    @Test
    public void shouldLogTasksInHistory() {
        ArrayList<Task> history = new ArrayList<>();

        int taskId = taskManager.addTask(new Task("Test task",
                "description", TEST_START_TIME, Duration.ofDays(1)));
        int epicId = taskManager.addEpic(new Epic("Test epic #", "description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask",
                "description", TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId));

        history.add(taskManager.getTask(taskId));
        history.add(taskManager.getEpic(epicId));
        history.add(taskManager.getSubtask(subtaskId));

        assertEquals(history, taskManager.getHistory(), "Task history mismatch");
    }

    @Test
    public void shouldUpdateHistoryAfterTaskRemoval() {
        int taskId = taskManager.addTask(new Task("Test task", "description",
                TEST_START_TIME, TEST_DURATION));
        taskManager.getTask(taskId);
        taskManager.removeTask(taskId);

        assertTrue(taskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void shouldUpdateHistoryAfterEpicRemoval() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask", "description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);
        taskManager.removeEpic(epicId);
        assertTrue(taskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void shouldUpdateHistoryAfterSubtaskRemoval() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask", "description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.getSubtask(subtaskId);
        taskManager.removeSubtask(subtaskId);
        assertTrue(taskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void shouldUpdateHistoryAfterTasksClearing() {
        ArrayList<Task> history = new ArrayList<>();

        int taskId = taskManager.addTask(new Task("Test task", "description",
                TEST_START_TIME, Duration.ofDays(1)));
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask",
                "description", TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId));

        taskManager.getTask(taskId);
        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);

        history.add(taskManager.getEpic(epicId));
        history.add(taskManager.getSubtask(subtaskId));
        taskManager.clearTasks();
        assertEquals(history, taskManager.getHistory(), "Task history mismatch");

        history.clear();
        history.add(taskManager.getEpic(epicId));
        taskManager.clearSubtasks();
        assertEquals(history, taskManager.getHistory(), "Task history mismatch");

        taskManager.clearEpics();
        assertTrue(taskManager.getHistory().isEmpty(), "Task history mismatch");
    }

    @Test
    public void updateEpicTimingAfterSubtaskAdding() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        taskManager.addSubtask(new Subtask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(5), epicId));
        taskManager.addSubtask(new Subtask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        taskManager.addSubtask(new Subtask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));

        assertEquals(TEST_START_TIME, taskManager.getEpic(epicId).getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(21), taskManager.getEpic(epicId).getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(21), taskManager.getEpic(epicId).getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterSubtaskRemoval() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(5), epicId));
        taskManager.addSubtask(new Subtask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        taskManager.addSubtask(new Subtask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        taskManager.removeSubtask(subtaskId);

        assertEquals(TEST_START_TIME.plusDays(5), taskManager.getEpic(epicId).getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(16), taskManager.getEpic(epicId).getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(21), taskManager.getEpic(epicId).getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterSubtaskUpdating() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(5), epicId));
        taskManager.addSubtask(new Subtask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        taskManager.addSubtask(new Subtask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        taskManager.updateSubtask(new Subtask(subtaskId, "Updated", "Updated", TaskStatus.DONE,
                TEST_START_TIME.plusDays(30), Duration.ofDays(1), epicId));

        assertEquals(TEST_START_TIME.plusDays(5), taskManager.getEpic(epicId).getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(26), taskManager.getEpic(epicId).getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(31), taskManager.getEpic(epicId).getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterSubtasksClearing() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        taskManager.addSubtask(new Subtask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(5), epicId));
        taskManager.addSubtask(new Subtask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        taskManager.addSubtask(new Subtask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        taskManager.clearSubtasks();

        assertTrue(taskManager.getEpic(epicId).getStartTime().isEmpty(), "Epic start time should be unknown");
        assertEquals(Duration.ZERO, taskManager.getEpic(epicId).getDuration(),
                "Epic duration should be 0");
        assertTrue(taskManager.getEpic(epicId).getEndTime().isEmpty(), "Epic end time should be unknown");
    }

    /// ///////////////////////////
    /// task priority tests
    @Test
    public void getPrioritizedTasks() {
        int taskId1 = taskManager.addTask(new Task("Test task #1", "description",
                TEST_START_TIME.plusDays(2), Duration.ofDays(1)));
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subtaskId1 = taskManager.addSubtask(new Subtask("Test subtask #1",
                "description", TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId));
        int subtaskId2 = taskManager.addSubtask(new Subtask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(3), Duration.ofDays(1), epicId));
        int taskId2 = taskManager.addTask(new Task("Test task #2", "description",
                TEST_START_TIME, Duration.ofDays(1)));
        int taskId3 = taskManager.addTask(new Task("Test task #3", "description",
                TEST_START_TIME.plusDays(6), Duration.ofDays(1)));
        int subtaskId3 = taskManager.addSubtask(new Subtask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(7), Duration.ofDays(1), epicId));
        taskManager.addTask(new Task("Test task #4", "description", null, Duration.ZERO));
        taskManager.addSubtask(new Subtask("Test subtask #4", "description", null, Duration.ZERO, epicId));


        List<? super Task> sortedTasks = Arrays.asList(
                taskManager.getTask(taskId2),
                taskManager.getSubtask(subtaskId1),
                taskManager.getTask(taskId1),
                taskManager.getSubtask(subtaskId2),
                taskManager.getTask(taskId3),
                taskManager.getSubtask(subtaskId3)
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.updateTask(new Task(taskId2, "Test task #2", "description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(4), Duration.ofDays(1)));
        sortedTasks = Arrays.asList(
                taskManager.getSubtask(subtaskId1),
                taskManager.getTask(taskId1),
                taskManager.getSubtask(subtaskId2),
                taskManager.getTask(taskId2),
                taskManager.getTask(taskId3),
                taskManager.getSubtask(subtaskId3)
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.updateTask(new Task(taskId2, "Test task #2", "description", TaskStatus.DONE,
                null, Duration.ZERO));
        sortedTasks = Arrays.asList(
                taskManager.getSubtask(subtaskId1),
                taskManager.getTask(taskId1),
                taskManager.getSubtask(subtaskId2),
                taskManager.getTask(taskId3),
                taskManager.getSubtask(subtaskId3)
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.updateSubtask(new Subtask(subtaskId2, "Test subtask #2", "description", TaskStatus.DONE,
                TEST_START_TIME, Duration.ofDays(1), epicId));
        sortedTasks = Arrays.asList(
                taskManager.getSubtask(subtaskId2),
                taskManager.getSubtask(subtaskId1),
                taskManager.getTask(taskId1),
                taskManager.getTask(taskId3),
                taskManager.getSubtask(subtaskId3)
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.updateSubtask(new Subtask(subtaskId3, "Test subtask #3", "description", TaskStatus.DONE,
                null, Duration.ZERO, epicId));
        sortedTasks = Arrays.asList(
                taskManager.getSubtask(subtaskId2),
                taskManager.getSubtask(subtaskId1),
                taskManager.getTask(taskId1),
                taskManager.getTask(taskId3)
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.removeTask(taskId1);
        sortedTasks = Arrays.asList(
                taskManager.getSubtask(subtaskId2),
                taskManager.getSubtask(subtaskId1),
                taskManager.getTask(taskId3)
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.removeSubtask(subtaskId1);
        sortedTasks = Arrays.asList(
                taskManager.getSubtask(subtaskId2),
                taskManager.getTask(taskId3)
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.clearSubtasks();
        sortedTasks = Arrays.asList(
                taskManager.getTask(taskId3)
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.clearTasks();
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(), "Task priority mismatch");
    }

    /// ///////////////////////
    /// time conflict tests
    @Test
    public void shouldAddTaskWithoutTimeIntersection() {
        Task task1 = new Task("Test task #1", "description", TEST_START_TIME.plusDays(2), Duration.ofDays(2));
        Task task2 = new Task("Test task #2", "description", TEST_START_TIME.plusDays(5), Duration.ofDays(2));
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        List<Task> tasks = taskManager.getTaskList();
        assertEquals(List.of(task1, task2), tasks, "Task list is wrong");
    }

    @Test
    public void shouldAddTaskWithCommonRightTimeBorder() {
        Task task1 = new Task("Test task #1", "description", TEST_START_TIME.plusDays(2), Duration.ofDays(2));
        Task task2 = new Task("Test task #2", "description", TEST_START_TIME.plusDays(4), Duration.ofDays(2));
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        List<Task> tasks = taskManager.getTaskList();
        assertEquals(List.of(task1, task2), tasks, "Task list is wrong");
    }

    @Test
    public void shouldAddTaskWithCommonLeftTimeBorder() {
        Task task1 = new Task("Test task #1", "description", TEST_START_TIME.plusDays(2), Duration.ofDays(2));
        Task task2 = new Task("Test task #2", "description", TEST_START_TIME.plusDays(0), Duration.ofDays(2));
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        List<Task> tasks = taskManager.getTaskList();
        assertEquals(List.of(task1, task2), tasks, "Task list is wrong");
    }


    @Test
    public void shouldNotAddTaskWithRightTimeIntersection() {
        Task task1 = new Task("Test task #1", "description", TEST_START_TIME.plusDays(2), Duration.ofDays(2));
        Task task2 = new Task("Test task #2", "description", TEST_START_TIME.plusDays(3), Duration.ofDays(2));
        taskManager.addTask(task1);
        assertThrows(TimeConflictException.class, () -> taskManager.addTask(task2),
                "Should throw exception for time conflict");

        List<Task> tasks = taskManager.getTaskList();
        assertEquals(List.of(task1), tasks, "Task list is wrong");
    }

    @Test
    public void shouldNotAddTaskWithLeftTimeIntersection() {
        Task task1 = new Task("Test task #1", "description", TEST_START_TIME.plusDays(2), Duration.ofDays(2));
        Task task2 = new Task("Test task #2", "description", TEST_START_TIME.plusDays(1), Duration.ofDays(2));
        taskManager.addTask(task1);
        assertThrows(TimeConflictException.class, () -> taskManager.addTask(task2),
                "Should throw exception for time conflict");

        List<Task> tasks = taskManager.getTaskList();
        assertEquals(List.of(task1), tasks, "Task list is wrong");
    }

}
