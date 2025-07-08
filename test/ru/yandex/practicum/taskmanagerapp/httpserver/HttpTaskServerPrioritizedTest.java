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

public class HttpTaskServerPrioritizedTest {
    TaskManager taskManager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    HttpClient client = HttpClient.newHttpClient();

    Gson gson = HttpTaskServer.getGson();

    class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    protected static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    protected static final Duration TEST_DURATION = Duration.ofDays(1);

    public HttpTaskServerPrioritizedTest() throws IOException {
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
        int taskId1 = taskManager.addTask(new Task("Test task #1", "Test task #1",
                TEST_START_TIME, Duration.ofDays(1)));
        int epicId = taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));
        int subtaskId1 = taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME.plusDays(1), Duration.ofDays(1), epicId));
        int taskId2 = taskManager.addTask(new Task("Test task #2", "Test task #2",
                TEST_START_TIME.plusDays(2), Duration.ofDays(1)));
        int subtaskId2 = taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME.plusDays(3), Duration.ofDays(1), epicId));


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        List<Task> prioritized = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(4, prioritized.size(), "Incorrect priority list");
        assertEquals(taskId1, prioritized.get(0).getId(), "Incorrect priority");
        assertEquals(subtaskId1, prioritized.get(1).getId(), "Incorrect priority");
        assertEquals(taskId2, prioritized.get(2).getId(), "Incorrect priority");
        assertEquals(subtaskId2, prioritized.get(3).getId(), "Incorrect priority");
    }
}