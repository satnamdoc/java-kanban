package ru.yandex.practicum.taskmanagerapp.taskmanager;

import org.junit.jupiter.api.BeforeEach;
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

class InMemoryTaskManagerTest {
    private static InMemoryTaskManager inMemoryTaskManager;
    private static final int TEST_ID = 0x10000000;
    private static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final Duration TEST_DURATION = Duration.ofDays(1).plusHours(1).plusMinutes(1);

    @BeforeEach
    public void beforeEach() {
        inMemoryTaskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
    }

    @Test
    void addTask() {
        Task task = new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION);
        inMemoryTaskManager.addTask(task);

        List<Task> tasks = inMemoryTaskManager.getTaskList();

        assertEquals(1, tasks.size(), "Wrong number of tasks");
        assertEquals(task, tasks.getFirst(), "Tasks are not equal");
    }

    @Test
    void addEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        inMemoryTaskManager.addEpic(epic);

        List<Epic> epics = inMemoryTaskManager.getEpicList();

        assertEquals(1, epics.size(), "Wrong number of epics");
        assertEquals(epic, epics.getFirst(), "Epics are not equal");
    }

    @Test
    void addSubTask() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask);

        List<SubTask> subTasks = inMemoryTaskManager.getSubTaskList();

        assertEquals(1, subTasks.size(), "Wrong number of subtasks");
        assertEquals(subTask, subTasks.getFirst(), "Subtasks are not equal");
    }

    @Test
    public void shouldNotAddNullTaskObject() {
        int taskId = inMemoryTaskManager.addTask(null);
        assertEquals(NULL_ID, taskId);
        assertTrue(inMemoryTaskManager.getTaskList().isEmpty(), "Task list should be empty");
    }

    @Test
    public void shouldNotAddNullEpicObject() {
        int epicId = inMemoryTaskManager.addEpic(null);

        assertEquals(NULL_ID, epicId);
        assertTrue(inMemoryTaskManager.getEpicList().isEmpty(), "Epic list should be empty");
    }

    @Test
    public void shouldNotAddEpicContainingSubTasks() {
        Epic epic = new Epic("Test epic", "Test epic description");
        epic.addSubTask(TEST_ID);
        int epicId = inMemoryTaskManager.addEpic(epic);

        assertEquals(NULL_ID, epicId);
        assertTrue(inMemoryTaskManager.getEpicList().isEmpty(), "Epic list should be empty");
    }

    @Test
    public void shouldNotAddNullSubTaskObject() {
        int subTaskId = inMemoryTaskManager.addSubTask(null);

        assertEquals(NULL_ID, subTaskId);
        assertTrue(inMemoryTaskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    public void shouldNotAddSubTaskForNonExistingEpic() {
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask",
                "Test subtask description", TEST_START_TIME, TEST_DURATION, TEST_ID));

        assertEquals(NULL_ID, subTaskId);
        assertTrue(inMemoryTaskManager.getSubTaskList().isEmpty());
    }

    @Test
    public void shouldNotUpdateNullTaskObject() {
        assertFalse(inMemoryTaskManager.updateTask(null),
                "updateTask should return false with null argument");
    }

    @Test
    public void shouldNotUpdateNonExistingTask() {
        Task task = new Task("Updated", "Updated", TEST_START_TIME, TEST_DURATION);
        task.setId(TEST_ID);

        assertFalse(inMemoryTaskManager.updateTask(task),
                "updateTask should return false when task doesn't exist");
    }

    @Test
    void updateTask() {
        Task task = new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION);
        int taskId = inMemoryTaskManager.addTask(task);

        Task updatedTask = new Task("Updated", "Updated", TEST_START_TIME.plusHours(1), TEST_DURATION.plusHours(1));
        updatedTask.setId(taskId);

        assertTrue(inMemoryTaskManager.updateTask(task));
        assertEquals(updatedTask, inMemoryTaskManager.getTask(taskId), "Tasks are not equal");
    }


    @Test
    public void shouldNotUpdateNullEpicObject() {
        assertFalse(inMemoryTaskManager.updateEpic(null),
                "updateEpic should return false with null argument");
    }

    @Test
    public void shouldNotUpdateNonExistingEpic() {
        Epic epic = new Epic("Updated", "Updated");
        epic.setId(TEST_ID);

        assertFalse(inMemoryTaskManager.updateEpic(epic),
                "updateEpic should return false when epic doesn't exist");
    }

    @Test
    public void shouldNotChangeEpicStatus() {
        Epic epic = new Epic("Test epic", "Test epic description");
        inMemoryTaskManager.addEpic(epic);

        Epic updatedEpic = new Epic("Updated", "Updated");
        updatedEpic.setStatus(TaskStatus.DONE);

        assertFalse(inMemoryTaskManager.updateEpic(updatedEpic),
                "updateEpic shouldn't change status directly");
    }


    @Test
    public void updateEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);

        Epic updatedEpic = new Epic("Updated", "Updated");
        updatedEpic.setId(epicId);

        assertTrue(inMemoryTaskManager.updateEpic(epic));
        assertEquals(updatedEpic, inMemoryTaskManager.getEpic(epicId), "Epics are not equal");
    }

    @Test
    public void shouldNotUpdateNullSubTaskObject() {
        assertFalse(inMemoryTaskManager.updateSubTask(null),
                "updateSubTask should return false with null argument");
    }

    @Test
    public void shouldNotUpdateNonExistingSubTask() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        SubTask subTask = new SubTask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        subTask.setId(TEST_ID);

        assertFalse(inMemoryTaskManager.updateSubTask(subTask),
                "updateSubTask should return false when epic doesn't exist");
    }

    @Test
    public void updateSubTask() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        SubTask subTask = new SubTask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        int subTaskId = inMemoryTaskManager.addSubTask(subTask);

        SubTask updatedSubTask = new SubTask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        updatedSubTask.setId(subTaskId);

        assertTrue(inMemoryTaskManager.updateSubTask(subTask));
        assertEquals(updatedSubTask, inMemoryTaskManager.getSubTask(subTaskId),
                "Subtasks are not equal");
    }


    @Test
    void getSubTasksOfEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask);

        List<SubTask> subTasks = inMemoryTaskManager.getSubTasksOfEpic(epicId);

        assertEquals(1, subTasks.size(), "Wrong number of subtasks");
        assertEquals(subTask, subTasks.getFirst(), "Subtasks are not equal");
    }

    @Test
    void clear() {
        inMemoryTaskManager.addTask(new Task("Test task", "Test task description", TEST_START_TIME, TEST_DURATION));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        inMemoryTaskManager.clear();

        assertTrue(inMemoryTaskManager.getTaskList().isEmpty(), "Task list should be empty");
        assertTrue(inMemoryTaskManager.getEpicList().isEmpty(), "Epic list should be empty");
        assertTrue(inMemoryTaskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void clearTasks() {
        inMemoryTaskManager.addTask(new Task("Test task", "Test task description",
                TEST_START_TIME, TEST_DURATION));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        inMemoryTaskManager.clearTasks();

        assertTrue(inMemoryTaskManager.getTaskList().isEmpty(), "Task list should be empty");
    }

    @Test
    void shouldRemoveAllTasksFromEpicListAndSubTaskListAfterEpicListClearing() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        inMemoryTaskManager.clearEpics();

        assertTrue(inMemoryTaskManager.getEpicList().isEmpty(), "Epic list should be empty");
        assertTrue(inMemoryTaskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void clearSubTasks() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId));

        inMemoryTaskManager.clearSubTasks();

        assertTrue(inMemoryTaskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void shouldNotRemoveNonExistingTask() {
        assertFalse(inMemoryTaskManager.removeTask(TEST_ID),
                "removeTask should return false when task doesn't exist");
    }

    @Test
    void removeTask() {
        int taskId = inMemoryTaskManager.addTask(new Task("Test task", "Test task description",
                TEST_START_TIME, TEST_DURATION));
        assertTrue(inMemoryTaskManager.removeTask(taskId), "removeTask should return true");
        assertNull(inMemoryTaskManager.getTask(taskId), "Removed task has been returned");
    }

    @Test
    void shouldNotRemoveNonExistingEpic() {
        assertFalse(inMemoryTaskManager.removeEpic(TEST_ID),
                "removeEpic should return false when epic doesn't exist");
    }

    @Test
    void removeEpic() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask",
                "Test subtask description", TEST_START_TIME, TEST_DURATION, epicId));
        assertTrue(inMemoryTaskManager.removeEpic(epicId), "removeEpic should return true");
        assertNull(inMemoryTaskManager.getEpic(epicId), "Removed epic has been returned");
        assertNull(inMemoryTaskManager.getSubTask(subTaskId), "Subtask of the removed epic is still in the list");
    }


    @Test
    void shouldNotRemoveNonExistingSubTask() {
        assertFalse(inMemoryTaskManager.removeSubTask(TEST_ID),
                "removeSubTask should return false when the epic doesn't exist");
    }

    @Test
    void removeSubTask() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask",
                "Test subtask description", TEST_START_TIME, TEST_DURATION, epicId));
        assertTrue(inMemoryTaskManager.removeSubTask(subTaskId), "removeSubTask should return true");
        assertNull(inMemoryTaskManager.getSubTask(subTaskId), "Removed subtask has been returned");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubTasksAdding() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubTasksRemoval() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.updateSubTask(subTask1);

        inMemoryTaskManager.removeSubTask(subTask2.getId());
        inMemoryTaskManager.removeSubTask(subTask1.getId());

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubTaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.updateSubTask(subTask1);
        subTask1.setStatus(TaskStatus.NEW);
        inMemoryTaskManager.updateSubTask(subTask1);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }


    @Test
    public void shouldSetEpicStatusToInProgressAfterSubTaskAdding() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        subTask1.setStatus(TaskStatus.DONE);
        inMemoryTaskManager.updateSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Status is not IN_PRPGRESS");
    }

    @Test
    public void shouldSetEpicStatusToInProgressAfterSubTaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.updateSubTask(subTask1);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Status is not IN_PRPGRESS");
    }

    @Test
    public void shouldSetEpicStatusToDoneAfterSubTaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.DONE);
        inMemoryTaskManager.updateSubTask(subTask1);
        subTask2.setStatus(TaskStatus.DONE);
        inMemoryTaskManager.updateSubTask(subTask2);

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Status is not DONE");
    }

    @Test
    public void shouldSetEpicStatusToDoneAfterSubTaskRemoval() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask", "Test subtask description",
                TEST_START_TIME, TEST_DURATION, epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.DONE);
        inMemoryTaskManager.updateSubTask(subTask1);
        inMemoryTaskManager.removeSubTask(subTask2.getId());

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Status is not DONE");
    }

    @Test
    public void shouldLogTasksInHistory() {
        ArrayList<Task> history = new ArrayList<>();

        int taskId = inMemoryTaskManager.addTask(new Task("Test task",
                "description", TEST_START_TIME, TEST_DURATION));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic #", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask",
                "description", TEST_START_TIME, TEST_DURATION, epicId));

        history.add(inMemoryTaskManager.getTask(taskId));
        history.add(inMemoryTaskManager.getEpic(epicId));
        history.add(inMemoryTaskManager.getSubTask(subTaskId));

        assertEquals(history, inMemoryTaskManager.getHistory(), "Task history mismatch");
    }

    @Test
    public void shouldUpdateHistoryAfterTaskRemoval() {
        int taskId = inMemoryTaskManager.addTask(new Task("Test task", "description",
                TEST_START_TIME, TEST_DURATION));
        inMemoryTaskManager.getTask(taskId);
        inMemoryTaskManager.removeTask(taskId);

        assertTrue(inMemoryTaskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void shouldUpdateHistoryAfterEpicRemoval() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask", "description",
                TEST_START_TIME, TEST_DURATION, epicId));

        inMemoryTaskManager.getEpic(epicId);
        inMemoryTaskManager.getSubTask(subTaskId);
        inMemoryTaskManager.removeEpic(epicId);
        assertTrue(inMemoryTaskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void shouldUpdateHistoryAfterSubtaskRemoval() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask", "description",
                TEST_START_TIME, TEST_DURATION, epicId));

        inMemoryTaskManager.getSubTask(subTaskId);
        inMemoryTaskManager.removeSubTask(subTaskId);
        assertTrue(inMemoryTaskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void shouldUpdateHistoryAfterTasksClearing() {
        ArrayList<Task> history = new ArrayList<>();

        int taskId = inMemoryTaskManager.addTask(new Task("Test task", "description",
                TEST_START_TIME, TEST_DURATION));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask",
                "description", TEST_START_TIME, TEST_DURATION, epicId));

        inMemoryTaskManager.getEpic(taskId);
        history.add(inMemoryTaskManager.getEpic(epicId));
        history.add(inMemoryTaskManager.getSubTask(subTaskId));
        inMemoryTaskManager.clearTasks();
        assertEquals(history, inMemoryTaskManager.getHistory(), "Task history mismatch");

        history.clear();
        history.add(inMemoryTaskManager.getEpic(epicId));
        inMemoryTaskManager.clearSubTasks();
        assertEquals(history, inMemoryTaskManager.getHistory(), "Task history mismatch");

        inMemoryTaskManager.clearEpics();
        assertTrue(inMemoryTaskManager.getHistory().isEmpty(), "Task history mismatch");
    }

    @Test
    public void updateEpicTimingAfterAddSubTask() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(10), epicId));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));

        assertEquals(TEST_START_TIME, inMemoryTaskManager.getEpic(epicId).getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(21), inMemoryTaskManager.getEpic(epicId).getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(21), inMemoryTaskManager.getEpic(epicId).getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterRemoveSubTask() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(10), epicId));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        inMemoryTaskManager.removeSubTask(subTaskId);

        assertEquals(TEST_START_TIME.plusDays(5), inMemoryTaskManager.getEpic(epicId).getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(11), inMemoryTaskManager.getEpic(epicId).getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(21), inMemoryTaskManager.getEpic(epicId).getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterUpdateSubTask() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(10), epicId));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        inMemoryTaskManager.updateSubTask(new SubTask(subTaskId, "Updated", "Updated", TaskStatus.DONE,
                TEST_START_TIME.plusDays(30), Duration.ofDays(5), epicId));

        assertEquals(TEST_START_TIME.plusDays(5), inMemoryTaskManager.getEpic(epicId).getStartTime().get(),
                "Epic start time mismatch");
        assertEquals(Duration.ofDays(16), inMemoryTaskManager.getEpic(epicId).getDuration(),
                "Epic duration mismatch");
        assertEquals(TEST_START_TIME.plusDays(35), inMemoryTaskManager.getEpic(epicId).getEndTime().get(),
                "Epic end time mismatch");
    }

    @Test
    public void updateEpicTimingAfterClearSubTasks() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME, Duration.ofDays(10), epicId));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(5), Duration.ofDays(10), epicId));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(20), Duration.ofDays(1), epicId));
        inMemoryTaskManager.clearSubTasks();

        assertTrue(inMemoryTaskManager.getEpic(epicId).getStartTime().isEmpty(), "Epic start time should be unknown");
        assertEquals(Duration.ZERO, inMemoryTaskManager.getEpic(epicId).getDuration(),
                "Epic duration should be 0");
        assertTrue(inMemoryTaskManager.getEpic(epicId).getEndTime().isEmpty(), "Epic end time should be unknown");
    }

    @Test
    public void getPrioritizedTasks() {
        int taskId1 = inMemoryTaskManager.addTask(new Task("Test task #1", "description",
                TEST_START_TIME.plusDays(2), TEST_DURATION));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId1 = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #1",
                "description", TEST_START_TIME.plusDays(1), TEST_DURATION, epicId));
        int subTaskId2 = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #2",
                "description", TEST_START_TIME.plusDays(3), TEST_DURATION, epicId));
        int taskId2 = inMemoryTaskManager.addTask(new Task("Test task #2", "description",
                TEST_START_TIME, TEST_DURATION));
        int taskId3 = inMemoryTaskManager.addTask(new Task("Test task #3", "description",
                TEST_START_TIME.plusDays(6), TEST_DURATION));
        int subTaskId3 = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #3",
                "description", TEST_START_TIME.plusDays(7), TEST_DURATION, epicId));
        inMemoryTaskManager.addTask(new Task("Test task #4", "description", null, Duration.ZERO));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask #4", "description", null, Duration.ZERO, epicId));


        List<? super Task> sortedTasks = Arrays.asList(
                inMemoryTaskManager.getTask(taskId2),
                inMemoryTaskManager.getSubTask(subTaskId1),
                inMemoryTaskManager.getTask(taskId1),
                inMemoryTaskManager.getSubTask(subTaskId2),
                inMemoryTaskManager.getTask(taskId3),
                inMemoryTaskManager.getSubTask(subTaskId3)
            );
        assertEquals(sortedTasks, inMemoryTaskManager.getPrioritizedTasks(), "Task priority mismatch");

        inMemoryTaskManager.updateTask(new Task(taskId2, "Test task #2", "description", TaskStatus.DONE,
                TEST_START_TIME.plusDays(4), TEST_DURATION));
        sortedTasks = Arrays.asList(
                inMemoryTaskManager.getSubTask(subTaskId1),
                inMemoryTaskManager.getTask(taskId1),
                inMemoryTaskManager.getSubTask(subTaskId2),
                inMemoryTaskManager.getTask(taskId2),
                inMemoryTaskManager.getTask(taskId3),
                inMemoryTaskManager.getSubTask(subTaskId3)
            );
        assertEquals(sortedTasks, inMemoryTaskManager.getPrioritizedTasks(), "Task priority mismatch");

        inMemoryTaskManager.updateTask(new Task(taskId2, "Test task #2", "description", TaskStatus.DONE,
                null, Duration.ZERO));
        sortedTasks = Arrays.asList(
                inMemoryTaskManager.getSubTask(subTaskId1),
                inMemoryTaskManager.getTask(taskId1),
                inMemoryTaskManager.getSubTask(subTaskId2),
                inMemoryTaskManager.getTask(taskId3),
                inMemoryTaskManager.getSubTask(subTaskId3)
        );
        assertEquals(sortedTasks, inMemoryTaskManager.getPrioritizedTasks(), "Task priority mismatch");

        inMemoryTaskManager.updateSubTask(new SubTask(subTaskId2, "Test subtask #2", "description", TaskStatus.DONE,
                TEST_START_TIME, TEST_DURATION, epicId));
        sortedTasks = Arrays.asList(
                inMemoryTaskManager.getSubTask(subTaskId2),
                inMemoryTaskManager.getSubTask(subTaskId1),
                inMemoryTaskManager.getTask(taskId1),
                inMemoryTaskManager.getTask(taskId3),
                inMemoryTaskManager.getSubTask(subTaskId3)
        );
        assertEquals(sortedTasks, inMemoryTaskManager.getPrioritizedTasks(), "Task priority mismatch");

        inMemoryTaskManager.updateSubTask(new SubTask(subTaskId3, "Test subtask #3", "description", TaskStatus.DONE,
                null, Duration.ZERO, epicId));
        sortedTasks = Arrays.asList(
                inMemoryTaskManager.getSubTask(subTaskId2),
                inMemoryTaskManager.getSubTask(subTaskId1),
                inMemoryTaskManager.getTask(taskId1),
                inMemoryTaskManager.getTask(taskId3)
        );
        assertEquals(sortedTasks, inMemoryTaskManager.getPrioritizedTasks(), "Task priority mismatch");

        inMemoryTaskManager.removeTask(taskId1);
        sortedTasks = Arrays.asList(
                inMemoryTaskManager.getSubTask(subTaskId2),
                inMemoryTaskManager.getSubTask(subTaskId1),
                inMemoryTaskManager.getTask(taskId3)
        );
        assertEquals(sortedTasks, inMemoryTaskManager.getPrioritizedTasks(), "Task priority mismatch");

        inMemoryTaskManager.removeSubTask(subTaskId1);
        sortedTasks = Arrays.asList(
                inMemoryTaskManager.getSubTask(subTaskId2),
                inMemoryTaskManager.getTask(taskId3)
        );
        assertEquals(sortedTasks, inMemoryTaskManager.getPrioritizedTasks(), "Task priority mismatch");

        inMemoryTaskManager.clearSubTasks();
        sortedTasks = Arrays.asList(
                inMemoryTaskManager.getTask(taskId3)
        );
        assertEquals(sortedTasks, inMemoryTaskManager.getPrioritizedTasks(), "Task priority mismatch");

        inMemoryTaskManager.clearTasks();
        assertTrue(inMemoryTaskManager.getPrioritizedTasks().isEmpty(), "Task priority mismatch");
    }
}