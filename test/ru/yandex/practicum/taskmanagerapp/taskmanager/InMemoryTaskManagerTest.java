package ru.yandex.practicum.taskmanagerapp.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.taskmanagerapp.task.Task.NULL_ID;

class InMemoryTaskManagerTest {
    private static InMemoryTaskManager inMemoryTaskManager;
    private static final int TEST_ID = 0x10000000;

    @BeforeEach
    public void beforeEach() {
        inMemoryTaskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
    }

    @Test
    void addTask() {
        Task task = new Task("Test task", "Test task description");
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
        SubTask subTask = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask);

        List<SubTask> subTasks = inMemoryTaskManager.getSubTaskList();

        assertEquals(1, subTasks.size(), "Wrong number of subtasks");
        assertEquals(subTask, subTasks.getFirst(),  "Subtasks are not equal");
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
                "Test subtask description", TEST_ID));

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
        Task task = new Task("Updated", "Updated");
        task.setId(TEST_ID);

        assertFalse(inMemoryTaskManager.updateTask(task),
                "updateTask should return false when task doesn't exist");
    }

    @Test
    void updateTask() {
        Task task = new Task("Test task", "Test task description");
        int taskId = inMemoryTaskManager.addTask(task);

        Task updatedTask = new Task("Updated", "Updated");
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

        assertFalse(inMemoryTaskManager.updateTask(epic),
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
        SubTask subTask = new SubTask("Updated", "Updated", epicId);
        subTask.setId(TEST_ID);

        assertFalse(inMemoryTaskManager.updateSubTask(subTask),
                "updateSubTask should return false when epic doesn't exist");
    }

    @Test
    public void updateSubTask() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        SubTask subTask = new SubTask("Updated", "Updated", epicId);
        int subTaskId = inMemoryTaskManager.addSubTask(subTask);

        SubTask updatedSubTask = new SubTask("Updated", "Updated", epicId);
        updatedSubTask.setId(subTaskId);

        assertTrue(inMemoryTaskManager.updateSubTask(subTask));
        assertEquals(updatedSubTask, inMemoryTaskManager.getSubTask(subTaskId),
                "Subtasks are not equal");
    }



    @Test
    void getSubTasksOfEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask);

        List<SubTask> subTasks = inMemoryTaskManager.getSubTasksOfEpic(epicId);

        assertEquals(1, subTasks.size(), "Wrong number of subtasks");
        assertEquals(subTask, subTasks.getFirst(), "Subtasks are not equal");
    }

    @Test
    void clear() {
        inMemoryTaskManager.addTask(new Task("Test task", "Test task description"));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask","Test subtask description", epicId));

        inMemoryTaskManager.clear();

        assertTrue(inMemoryTaskManager.getTaskList().isEmpty(), "Task list should be empty");
        assertTrue(inMemoryTaskManager.getEpicList().isEmpty(), "Epic list should be empty");
        assertTrue(inMemoryTaskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void clearTasks() {
        inMemoryTaskManager.addTask(new Task("Test task", "Test task description"));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask","Test subtask description", epicId));

        inMemoryTaskManager.clearTasks();

        assertTrue(inMemoryTaskManager.getTaskList().isEmpty(), "Task list should be empty");
    }

    @Test
    void shouldRemoveAllTasksFromEpicListAndSubTaskListAfterEpicListClearing() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask","Test subtask description", epicId));

        inMemoryTaskManager.clearEpics();

        assertTrue(inMemoryTaskManager.getEpicList().isEmpty(), "Epic list should be empty");
        assertTrue(inMemoryTaskManager.getSubTaskList().isEmpty(), "Subtask list should be empty");
    }

    @Test
    void clearSubTasks() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic", "Test epic description"));
        inMemoryTaskManager.addSubTask(new SubTask("Test subtask","Test subtask description", epicId));

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
        int taskId = inMemoryTaskManager.addTask(new Task("Test task", "Test task description"));
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
                "Test subtask description", epicId));
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
                "Test subtask description", epicId));
        assertTrue(inMemoryTaskManager.removeSubTask(subTaskId), "removeSubTask should return true");
        assertNull(inMemoryTaskManager.getSubTask(subTaskId), "Removed subtask has been returned");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubTasksAdding() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Status is not NEW");
    }

    @Test
    public void shouldSetEpicStatusToNewAfterSubTasksRemoval() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask","Test subtask description", epicId);
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
        SubTask subTask1 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask","Test subtask description", epicId);
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
        SubTask subTask1 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        subTask1.setStatus(TaskStatus.DONE);
        inMemoryTaskManager.updateSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask2);
        
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Status is not IN_PRPGRESS");
    }

    @Test
    public void shouldSetEpicStatusToInProgressAfterSubTaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.updateSubTask(subTask1);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Status is not IN_PRPGRESS");
    }

    @Test
    public void shouldSetEpicStatusToDoneAfterSubTaskUpdation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = inMemoryTaskManager.addEpic(epic);
        SubTask subTask1 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask","Test subtask description", epicId);
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
        SubTask subTask1 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test subtask","Test subtask description", epicId);
        inMemoryTaskManager.addSubTask(subTask2);

        subTask1.setStatus(TaskStatus.DONE);
        inMemoryTaskManager.updateSubTask(subTask1);
        inMemoryTaskManager.removeSubTask(subTask2.getId());

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Status is not DONE");
    }

    @Test
    public void souldLogTasksInHistory() {
        ArrayList<Task> history = new ArrayList<>();

        int taskId = inMemoryTaskManager.addTask(new Task("Test task #", "description"));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic #", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #", "description", epicId));

        history.add(inMemoryTaskManager.getTask(taskId));
        history.add(inMemoryTaskManager.getEpic(epicId));
        history.add(inMemoryTaskManager.getSubTask(subTaskId));

        assertEquals(history, inMemoryTaskManager.getHistory(), "Task history mismatch");
    }

    @Test
    public void souldUpdateHistoryAfterTaskRemoval() {
        int taskId = inMemoryTaskManager.addTask(new Task("Test task #", "description"));
        inMemoryTaskManager.getTask(taskId);
        inMemoryTaskManager.removeTask(taskId);

        assertTrue(inMemoryTaskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void souldUpdateHistoryAfterEpicRemoval() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic #", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #", "description", epicId));

        inMemoryTaskManager.getEpic(epicId);
        inMemoryTaskManager.getSubTask(subTaskId);
        inMemoryTaskManager.removeEpic(epicId);
        assertTrue(inMemoryTaskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void souldUpdateHistoryAfterSubtaskRemoval() {
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic #", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #", "description", epicId));

        inMemoryTaskManager.getSubTask(subTaskId);
        inMemoryTaskManager.removeSubTask(subTaskId);
        assertTrue(inMemoryTaskManager.getHistory().isEmpty(),
                "All tasks should be deleted from history");
    }

    @Test
    public void souldUpdateHistoryAfterTasksClearing() {
        ArrayList<Task> history = new ArrayList<>();

        int taskId = inMemoryTaskManager.addTask(new Task("Test task #", "description"));
        int epicId = inMemoryTaskManager.addEpic(new Epic("Test epic #", "description"));
        int subTaskId = inMemoryTaskManager.addSubTask(new SubTask("Test subtask #", "description", epicId));

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
}