package ru.yandex.practicum.taskmanagerapp.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.exception.ManagerLoadException;
import ru.yandex.practicum.taskmanagerapp.exception.ManagerSaveException;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private static File tempFile;
    private static final String CSVFILE_HEADER = "id,type,status,name,description,start time,duration,epic";

    @BeforeEach
    public void beforeEach() throws IOException {
        tempFile = File.createTempFile("testtmdata", ".tmp");
        tempFile.deleteOnExit();
        taskManager = new FileBackedTaskManager(tempFile, Managers.getDefaultHistory());
        taskManager.clear();  // trigger file saving
    }

    @Test
    void saveEmptyTaskManager() throws IOException {
        String fileData = Files.readString(Paths.get(tempFile.getAbsolutePath()));
        assertEquals(CSVFILE_HEADER + "\n", fileData, "Data file should contain CVS header");
    }

    @Test
    void loadEmptyTaskManager() {
        FileBackedTaskManager tm = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(tm.getTaskList().isEmpty(), "Task manager has a task");
        assertTrue(tm.getEpicList().isEmpty(), "Task manager has an epic");
        assertTrue(tm.getSubtaskList().isEmpty(), "Task manager has a subtask");
    }

    @Test
    void saveTaskManagerToDataFile() throws IOException {
        int taskId = taskManager.addTask(new Task("Test task", "description",
                TEST_START_TIME, TEST_DURATION));
        int epicId = taskManager.addEpic(new Epic("Test epic", "description"));
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask", "description",
                TEST_START_TIME.plus(TEST_DURATION), TEST_DURATION, epicId));

        String fileData = CSVFILE_HEADER + "\n" +
                taskId + ",TASK,NEW,Test task,description,01.01.2025 00:00,1440,\n" +
                epicId + ",EPIC,NEW,Test epic,description,02.01.2025 00:00,1440,\n" +
                subtaskId + ",SUBTASK,NEW,Test subtask,description,02.01.2025 00:00,1440," + epicId + "\n";
        assertEquals(fileData, Files.readString(Paths.get(tempFile.getAbsolutePath())),
                "Data file corruption");
    }

    @Test
    void loadTaskManagerFromDataFile() {
        Task task = new Task("Test task", "description", TEST_START_TIME, TEST_DURATION);
        taskManager.addTask(task);
        Epic epic = new Epic("Test epic", "description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test subtask", "description",
                TEST_START_TIME.plus(TEST_DURATION), TEST_DURATION, epicId);
        taskManager.addSubtask(subtask);

        FileBackedTaskManager tm = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(List.of(task), tm.getTaskList(), "Task list mismatch");
        assertEquals(List.of(
                        new Epic(epicId, "Test epic", "description",
                                TaskStatus.NEW, TEST_START_TIME.plus(TEST_DURATION), TEST_DURATION,
                                TEST_START_TIME.plus(TEST_DURATION).plus(TEST_DURATION),
                                List.of(subtask.getId())
                                )
                        ),
                        tm.getEpicList(), "Epic list mismatch");
        assertEquals(List.of(subtask), tm.getSubtaskList(), "Subtask list mismatch");
    }

    @Test
    public void saveToBadDataFile() {
        taskManager = new FileBackedTaskManager(new File("bad\\//bad"), Managers.getDefaultHistory());
        assertThrows(ManagerSaveException.class, taskManager::clear, "Bad file saving should throw exception");
    }

    @Test
    public void loadFromBadDataFile() {
        assertThrows(ManagerLoadException.class, () -> FileBackedTaskManager.loadFromFile(new File("bad\\bad")),
                "Bad file loading should throw exception");
    }
}