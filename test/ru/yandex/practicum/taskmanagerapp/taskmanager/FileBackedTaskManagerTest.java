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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private static FileBackedTaskManager fileBackedTaskManager;
    private static File tempFile;
    private static final String CSVFILE_HEADER = "id,type,name,status,description,epic";

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
        int taskId = fileBackedTaskManager.addTask(new Task("Test task", "description"));
        int epicId = fileBackedTaskManager.addEpic(new Epic("Test epic", "description"));
        int subTaskId = fileBackedTaskManager.addSubTask(new SubTask("Test subtask", "description", epicId));

        String fileData = CSVFILE_HEADER + "\n" +
                taskId + ",TASK,NEW,Test task,description,\n" +
                epicId + ",EPIC,NEW,Test epic,description,\n" +
                subTaskId + ",SUBTASK,NEW,Test subtask,description," + epicId + "\n";
        assertEquals(fileData, Files.readString(Paths.get(tempFile.getAbsolutePath())),
                    "Data file corruption");
    }

    @Test
    void loadTaskManagerFromDataFile() {
        Task task = new Task("Test task", "description");
        int taskId = fileBackedTaskManager.addTask(task);
        Epic epic = new Epic("Test epic", "description");
        int epicId = fileBackedTaskManager.addEpic(epic);
        SubTask subTask = new SubTask("Test subtask", "description", epicId);
        int subTaskId = fileBackedTaskManager.addSubTask(subTask);

        FileBackedTaskManager tm = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(List.of(task), tm.getTaskList(), "Task list mismatch");
        assertEquals(List.of(epic), tm.getEpicList(), "Epic list mismatch");
        assertEquals(List.of(subTask), tm.getSubTaskList(), "Subtask list mismatch");
    }
}