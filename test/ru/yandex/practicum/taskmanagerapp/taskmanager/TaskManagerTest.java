package ru.yandex.practicum.taskmanagerapp.taskmanager;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
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
    protected static final Duration TEST_DURATION = Duration.ofDays(1).plusHours(1).plusMinutes(1);

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
    void addSubTask() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        taskManager.addSubTask(subTask);

        List<SubTask> subTasks = taskManager.getSubTaskList();

        assertEquals(1, subTasks.size(), "Wrong number of subtasks");
        assertEquals(subTask, subTasks.getFirst(), "Subtasks are not equal");
    }

    @Test
    public void shouldNotAddNullTaskObject() {
        int taskId = taskManager.addTask(null);
        assertEquals(NULL_ID, taskId);
        assertTrue(taskManager.getTaskList().isEmpty(), "Task list should be empty");
    }

    @Test
    public void shouldNotAddNullEpicObject() {
        int epicId = taskManager.addEpic(null);

        assertEquals(NULL_ID, epicId);
        assertTrue(taskManager.getEpicList().isEmpty(), "Epic list should be empty");
    }

    @Test
    public void shouldNotAddEpicContainingSubTasks() {
        Epic epic = new Epic("Test epic", "Test epic description");
        epic.addSubTask(TEST_ID);
        int epicId = taskManager.addEpic(epic);

        assertEquals(NULL_ID, epicId);
        assertTrue(taskManager.getEpicList().isEmpty(), "Epic list should be empty");
    }

    @Test
    public void shouldNotAddNullSubTaskObject() {
        int subTaskId = taskManager.addSubTask(null);

        assertEquals(NULL_ID, subTaskId);
        assertTrue(taskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    public void shouldNotAddSubTaskForNonExistingEpic() {
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask",
                "Test subtask description", TEST_START_TIME, TEST_DURATION, TEST_ID));

        assertEquals(NULL_ID, subTaskId);
        assertTrue(taskManager.getSubTaskList().isEmpty());
    }

    @Test
    public void shouldNotUpdateNullTaskObject() {
        assertFalse(taskManager.updateTask(null),
                "updateTask should return false with null argument");
    }

    @Test
    public void shouldNotUpdateNonExistingTask() {
        Task task = new Task("Updated", "Updated", TEST_START_TIME, TEST_DURATION);
        task.setId(TEST_ID);

        assertFalse(taskManager.updateTask(task),
                "updateTask should return false when task doesn't exist");
    }

    @Test
    void updateTask() {
        Task task = new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION);
        int taskId = taskManager.addTask(task);

        Task updatedTask = new Task("Updated", "Updated", TEST_START_TIME, TEST_DURATION);
        updatedTask.setId(taskId);

        assertTrue(taskManager.updateTask(updatedTask));
        assertEquals(updatedTask, taskManager.getTask(taskId).get(), "Tasks are not equal");
    }


    @Test
    public void shouldNotUpdateNullEpicObject() {
        assertFalse(taskManager.updateEpic(null),
                "updateEpic should return false with null argument");
    }

    @Test
    public void shouldNotUpdateNonExistingEpic() {
        Epic epic = new Epic("Updated", "Updated");
        epic.setId(TEST_ID);

        assertFalse(taskManager.updateEpic(epic),
                "updateEpic should return false when epic doesn't exist");
    }

    @Test
    public void shouldNotChangeEpicStatus() {
        Epic epic = new Epic("Test epic", "Test epic description");
        taskManager.addEpic(epic);

        Epic updatedEpic = new Epic("Updated", "Updated");
        updatedEpic.setStatus(TaskStatus.DONE);

        assertFalse(taskManager.updateEpic(updatedEpic),
                "updateEpic shouldn't change status directly");
    }


    @Test
    public void updateEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);

        Epic updatedEpic = new Epic("Updated", "Updated");
        updatedEpic.setId(epicId);

        assertTrue(taskManager.updateEpic(updatedEpic));
        assertEquals(updatedEpic, taskManager.getEpic(epicId).get(), "Epics are not equal");
    }

    @Test
    public void shouldNotUpdateNullSubTaskObject() {
        assertFalse(taskManager.updateSubTask(null),
                "updateSubTask should return false with null argument");
    }

    @Test
    public void shouldNotUpdateNonExistingSubTask() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        SubTask subTask = new SubTask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        subTask.setId(TEST_ID);

        assertFalse(taskManager.updateSubTask(subTask),
                "updateSubTask should return false when epic doesn't exist");
    }

    @Test
    public void updateSubTask() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        SubTask subTask = new SubTask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        int subTaskId = taskManager.addSubTask(subTask);

        SubTask updatedSubTask = new SubTask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        updatedSubTask.setId(subTaskId);

        assertTrue(taskManager.updateSubTask(updatedSubTask));
        assertEquals(updatedSubTask, taskManager.getSubTask(subTaskId).get(),
                "Subtasks are not equal");
    }


    @Test
    void getEpicSubTasks() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        taskManager.addSubTask(subTask);

        List<SubTask> subTasks = taskManager.getEpicSubTasks(epicId);

        assertEquals(1, subTasks.size(), "Wrong number of subtasks");
        assertEquals(subTask, subTasks.getFirst(), "Subtasks are not equal");
    }

    @Test
    void clear() {
        taskManager.addTask(new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION));
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        taskManager.addSubTask(new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.clear();

        assertTrue(taskManager.getTaskList().isEmpty(), "Task list should be empty");
        assertTrue(taskManager.getEpicList().isEmpty(), "Epic list should be empty");
        assertTrue(taskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void clearTasks() {
        taskManager.addTask(new Task("Test task", "Test task description",
                TEST_START_TIME, TEST_DURATION));
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        taskManager.addSubTask(new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.clearTasks();

        assertTrue(taskManager.getTaskList().isEmpty(), "Task list should be empty");
    }

    @Test
    void shouldRemoveAllTasksFromEpicListAndSubTaskListAfterEpicListClearing() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        taskManager.addSubTask(new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.clearEpics();

        assertTrue(taskManager.getEpicList().isEmpty(), "Epic list should be empty");
        assertTrue(taskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void clearSubTasks() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        taskManager.addSubTask(new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.clearSubTasks();

        assertTrue(taskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void shouldNotRemoveNonExistingTask() {
        assertFalse(taskManager.removeTask(TEST_ID),
                "removeTask should return false when task doesn't exist");
    }

    @Test
    void removeTask() {
        int taskId = taskManager.addTask(new Task("Test task", "Test task description",
                TEST_START_TIME, TEST_DURATION));
        assertTrue(taskManager.removeTask(taskId), "removeTask should return true");
        assertTrue(taskManager.getTask(taskId).isEmpty(), "Removed task has been returned");
    }

    @Test
    void shouldNotRemoveNonExistingEpic() {
        assertFalse(taskManager.removeEpic(TEST_ID),
                "removeEpic should return false when epic doesn't exist");
    }

    @Test
    void removeEpic() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask",
                "Test subtask description", TEST_START_TIME, TEST_DURATION, epicId));
        assertTrue(taskManager.removeEpic(epicId), "removeEpic should return true");
        assertTrue(taskManager.getEpic(epicId).isEmpty(), "Removed epic has been returned");
        assertTrue(taskManager.getSubTask(subTaskId).isEmpty(), "Subtask of the removed epic is still in the list");
    }


    @Test
    void shouldNotRemoveNonExistingSubTask() {
        assertFalse(taskManager.removeSubTask(TEST_ID),
                "removeSubTask should return false when the epic doesn't exist");
    }

    @Test
    void removeSubTask() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "Test epic description"));
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask",
                "Test subtask description", TEST_START_TIME, TEST_DURATION, epicId));
        assertTrue(taskManager.removeSubTask(subTaskId), "removeSubTask should return true");
        assertTrue(taskManager.getSubTask(subTaskId).isEmpty(), "Removed subtask has been returned");
    }

    /// ////////////////////////
    /// Epic status management tests
    @Test
    public void shouldSetEpicStatusToNewWhenAllSubtasksAreNew() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(2), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask3);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }

    @Test
    public void shouldSetEpicStatusToNewWhenAllSubtasksAreDone() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(2), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask3);

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Status is not DONE");
    }

    @Test
    public void shouldSetEpicStatusToInProgressWhenSubtasksAreNewAndDone() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.NEW,
                TEST_START_TIME.plusDays(2), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask3);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Status is not IN_PROGRESS");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenOneSubtaskIsInProgress() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.DONE,
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.IN_PROGRESS,
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask(NULL_ID, "Test subtask", "Test subtask description", TaskStatus.NEW,
                TEST_START_TIME.plusDays(2), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask3);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Status is not IN PROGRESS");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubTasksRemoval() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTask1);

        taskManager.removeSubTask(subTask2.getId());
        taskManager.removeSubTask(subTask1.getId());

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubTaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTask1);
        subTask1.setStatus(TaskStatus.NEW);
        taskManager.updateSubTask(subTask1);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }


    @Test
    public void shouldSetEpicStatusToInProgressAfterSubTaskAdding() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        subTask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Status is not IN_PRPGRESS");
    }

    @Test
    public void shouldSetEpicStatusToInProgressAfterSubTaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTask1);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Status is not IN_PRPGRESS");
    }

    @Test
    public void shouldSetEpicStatusToDoneAfterSubTaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);
        subTask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask2);

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Status is not DONE");
    }

    @Test
    public void shouldSetEpicStatusToDoneAfterSubTaskRemoval() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId);
        taskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);
        taskManager.removeSubTask(subTask2.getId());

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Status is not DONE");
    }

    @Test
    public void shouldLogTasksInHistory() {
        ArrayList<Task> history = new ArrayList<>();

        int taskId = taskManager.addTask(new Task("Test task",
                "description", TEST_START_TIME, Duration.ofDays(1)));
        int epicId = taskManager.addEpic(new Epic("Test epic #", "description"));
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask",
                "description", TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId));

        history.add(taskManager.getTask(taskId).get());
        history.add(taskManager.getEpic(epicId).get());
        history.add(taskManager.getSubTask(subTaskId).get());

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
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask", "description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.getEpic(epicId);
        taskManager.getSubTask(subTaskId);
        taskManager.removeEpic(epicId);
        assertTrue(taskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void shouldUpdateHistoryAfterSubtaskRemoval() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask", "description",
                TEST_START_TIME, TEST_DURATION, epicId));

        taskManager.getSubTask(subTaskId);
        taskManager.removeSubTask(subTaskId);
        assertTrue(taskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void shouldUpdateHistoryAfterTasksClearing() {
        ArrayList<Task> history = new ArrayList<>();

        int taskId = taskManager.addTask(new Task("Test task", "description",
                TEST_START_TIME, Duration.ofDays(1)));
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask",
                "description", TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId));

        taskManager.getEpic(taskId);
        history.add(taskManager.getEpic(epicId).get());
        history.add(taskManager.getSubTask(subTaskId).get());
        taskManager.clearTasks();
        assertEquals(history, taskManager.getHistory(), "Task history mismatch");

        history.clear();
        history.add(taskManager.getEpic(epicId).get());
        taskManager.clearSubTasks();
        assertEquals(history, taskManager.getHistory(), "Task history mismatch");

        taskManager.clearEpics();
        assertTrue(taskManager.getHistory().isEmpty(), "Task history mismatch");
    }

    @Test
    public void updateEpicTimingAfterSubTaskAdding() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        taskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(5), epicId));
        taskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        taskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));

        assertEquals(TEST_START_TIME, taskManager.getEpic(epicId).get().getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(16), taskManager.getEpic(epicId).get().getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(21), taskManager.getEpic(epicId).get().getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterSubTaskRemoval() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(5), epicId));
        taskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        taskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        taskManager.removeSubTask(subTaskId);

        assertEquals(TEST_START_TIME.plusDays(5), taskManager.getEpic(epicId).get().getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(11), taskManager.getEpic(epicId).get().getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(21), taskManager.getEpic(epicId).get().getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterSubTaskUpdating() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = taskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(5), epicId));
        taskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        taskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        taskManager.updateSubTask(new SubTask(subTaskId, "Updated", "Updated", TaskStatus.DONE,
                TEST_START_TIME.plusDays(30), Duration.ofDays(1), epicId));

        assertEquals(TEST_START_TIME.plusDays(5), taskManager.getEpic(epicId).get().getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(12), taskManager.getEpic(epicId).get().getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(31), taskManager.getEpic(epicId).get().getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterSubTasksClearing() {
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        taskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(10), epicId));
        taskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        taskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        taskManager.clearSubTasks();

        assertTrue(taskManager.getEpic(epicId).get().getStartTime().isEmpty(), "Epic start time should be unknown");
        assertEquals(Duration.ZERO, taskManager.getEpic(epicId).get().getDuration(),
                "Epic duration should be 0");
        assertTrue(taskManager.getEpic(epicId).get().getEndTime().isEmpty(), "Epic end time should be unknown");
    }

    /// ///////////////////////////
    /// task priority tests
    @Test
    public void getPrioritizedTasks() {
        int taskId1 = taskManager.addTask(new Task("Test task #1", "description",
                TEST_START_TIME.plusDays(2), Duration.ofDays(1)));
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId1 = taskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId));
        int subTaskId2 = taskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(3), Duration.ofDays(1), epicId));
        int taskId2 = taskManager.addTask(new Task("Test task #2", "description",
                TEST_START_TIME, Duration.ofDays(1)));
        int taskId3 = taskManager.addTask(new Task("Test task #3", "description",
                TEST_START_TIME.plusDays(6), Duration.ofDays(1)));
        int subTaskId3 = taskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(7), Duration.ofDays(1), epicId));
        taskManager.addTask(new Task("Test task #4", "description", null, Duration.ZERO));
        taskManager.addSubTask(new SubTask("Test subtask #4", "description", null, Duration.ZERO, epicId));


        List<? super Task> sortedTasks = Arrays.asList(
                taskManager.getTask(taskId2).get(),
                taskManager.getSubTask(subTaskId1).get(),
                taskManager.getTask(taskId1).get(),
                taskManager.getSubTask(subTaskId2).get(),
                taskManager.getTask(taskId3).get(),
                taskManager.getSubTask(subTaskId3).get()
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.updateTask(new Task(taskId2, "Test task #2", "description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(4), Duration.ofDays(1)));
        sortedTasks = Arrays.asList(
                taskManager.getSubTask(subTaskId1).get(),
                taskManager.getTask(taskId1).get(),
                taskManager.getSubTask(subTaskId2).get(),
                taskManager.getTask(taskId2).get(),
                taskManager.getTask(taskId3).get(),
                taskManager.getSubTask(subTaskId3).get()
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.updateTask(new Task(taskId2, "Test task #2", "description", TaskStatus.DONE,
                null, Duration.ZERO));
        sortedTasks = Arrays.asList(
                taskManager.getSubTask(subTaskId1).get(),
                taskManager.getTask(taskId1).get(),
                taskManager.getSubTask(subTaskId2).get(),
                taskManager.getTask(taskId3).get(),
                taskManager.getSubTask(subTaskId3).get()
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.updateSubTask(new SubTask(subTaskId2, "Test subtask #2", "description", TaskStatus.DONE,
                TEST_START_TIME, Duration.ofDays(1), epicId));
        sortedTasks = Arrays.asList(
                taskManager.getSubTask(subTaskId2).get(),
                taskManager.getSubTask(subTaskId1).get(),
                taskManager.getTask(taskId1).get(),
                taskManager.getTask(taskId3).get(),
                taskManager.getSubTask(subTaskId3).get()
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.updateSubTask(new SubTask(subTaskId3, "Test subtask #3", "description", TaskStatus.DONE,
                null, Duration.ZERO, epicId));
        sortedTasks = Arrays.asList(
                taskManager.getSubTask(subTaskId2).get(),
                taskManager.getSubTask(subTaskId1).get(),
                taskManager.getTask(taskId1).get(),
                taskManager.getTask(taskId3).get()
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.removeTask(taskId1);
        sortedTasks = Arrays.asList(
                taskManager.getSubTask(subTaskId2).get(),
                taskManager.getSubTask(subTaskId1).get(),
                taskManager.getTask(taskId3).get()
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.removeSubTask(subTaskId1);
        sortedTasks = Arrays.asList(
                taskManager.getSubTask(subTaskId2).get(),
                taskManager.getTask(taskId3).get()
        );
        assertEquals(sortedTasks, taskManager.getPrioritizedTasks(), "Task priority mismatch");

        taskManager.clearSubTasks();
        sortedTasks = Arrays.asList(
                taskManager.getTask(taskId3).get()
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
        taskManager.addTask(task2);

        List<Task> tasks = taskManager.getTaskList();
        assertEquals(List.of(task1), tasks, "Task list is wrong");
    }

    @Test
    public void shouldNotAddTaskWithLeftTimeIntersection() {
        Task task1 = new Task("Test task #1", "description", TEST_START_TIME.plusDays(2), Duration.ofDays(2));
        Task task2 = new Task("Test task #2", "description", TEST_START_TIME.plusDays(1), Duration.ofDays(2));
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        List<Task> tasks = taskManager.getTaskList();
        assertEquals(List.of(task1), tasks, "Task list is wrong");
    }

}
