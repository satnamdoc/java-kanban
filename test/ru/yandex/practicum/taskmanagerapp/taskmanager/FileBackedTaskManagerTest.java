package ru.yandex.practicum.taskmanagerapp.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private static FileBackedTaskManager fileBackedTaskManager;
    private static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final Duration TEST_DURATION = Duration.ofDays(1).plusHours(1).plusMinutes(1);
    private static File tempFile;
    private static final String CSVFILE_HEADER = "id,type,name,status,description,epic,start time,duration";

    @BeforeEach
    public void beforeEach()  throws IOException {
            tempFile = File.createTempFile("testtmdata", ".tmp");
            tempFile.deleteOnExit();
            fileBackedTaskManager = new FileBackedTaskManager(tempFile, Managers.getDefaultHistory());
            fileBackedTaskManager.clear();  // trigger file saving
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
        assertTrue(tm.getSubTaskList().isEmpty(), "Task manager has a subtask");
    }


    @Test
    void saveTaskManagerToDataFile() throws IOException {
        int taskId = fileBackedTaskManager.addTask(new Task("Test task", "description",
                TEST_START_TIME, TEST_DURATION));
        int epicId = fileBackedTaskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = fileBackedTaskManager.addSubTask(new SubTask("Test subtask", "description",
                TEST_START_TIME, TEST_DURATION, epicId));

        String fileData = CSVFILE_HEADER + "\n" +
                taskId + ",TASK,NEW,Test task,description,01.01.2025 00:00,1501,\n" +
                epicId + ",EPIC,NEW,Test epic,description,01.01.2025 00:00,1501,\n" +
                subTaskId + ",SUBTASK,NEW,Test subtask,description,01.01.2025 00:00,1501," + epicId + "\n";
        assertEquals(fileData, Files.readString(Paths.get(tempFile.getAbsolutePath())),
                    "Data file corruption");
    }

    @Test
    void loadTaskManagerFromDataFile() {
        Task task = new Task("Test task", "description", TEST_START_TIME, TEST_DURATION);
        fileBackedTaskManager.addTask(task);
        Epic epic = new Epic("Test epic", "description");
        int epicId = fileBackedTaskManager.addEpic(epic);
        SubTask subTask = new SubTask("Test subtask", "description", TEST_START_TIME, TEST_DURATION, epicId);
        fileBackedTaskManager.addSubTask(subTask);

        FileBackedTaskManager tm = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(List.of(task), tm.getTaskList(), "Task list mismatch");
        assertEquals(List.of(epic), tm.getEpicList(), "Epic list mismatch");
        assertEquals(List.of(subTask), tm.getSubTaskList(), "Subtask list mismatch");
    }
}