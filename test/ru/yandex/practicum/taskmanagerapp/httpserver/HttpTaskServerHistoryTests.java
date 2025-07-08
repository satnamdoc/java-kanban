package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.taskmanager.InMemoryTaskManager;
import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerHistoryTests {
    TaskManager taskManager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    HttpClient client = HttpClient.newHttpClient();

    Gson gson = HttpTaskServer.getGson();

    class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    protected static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    protected static final Duration TEST_DURATION = Duration.ofDays(1);

    public HttpTaskServerHistoryTests() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        taskManager.clear();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void getHistoryTest() throws IOException, InterruptedException {
        Task task = new Task("Test task #1", "Test task #1", TEST_START_TIME, TEST_DURATION);
        int taskId = taskManager.addTask(task);
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME.plus(TEST_DURATION), TEST_DURATION, epicId));

        taskManager.getTask(taskId);
        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history/"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        List<Task> history = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(3, history.size(), "Incorrect history");
        assertEquals(taskId, history.get(0).getId(), "Incorrect history");
        assertEquals(epicId, history.get(1).getId(), "Incorrect history");
        assertEquals(subtaskId, history.get(2).getId(), "Incorrect history");
    }
}