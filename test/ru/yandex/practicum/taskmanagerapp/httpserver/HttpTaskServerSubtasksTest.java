package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;
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

public class HttpTaskServerSubtasksTest {
    TaskManager taskManager = new InMemoryTaskManager();
    HttpTaskServer epicServer = new HttpTaskServer(taskManager);
    HttpClient client = HttpClient.newHttpClient();

    Gson gson = HttpTaskServer.getGson();

    class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
    }

    protected static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    protected static final Duration TEST_DURATION = Duration.ofDays(1);

    public HttpTaskServerSubtasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        taskManager.clear();
        epicServer.start();
    }

    @AfterEach
    public void shutDown() {
        epicServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test subtask #1", "desc", TEST_START_TIME, TEST_DURATION, epicId);

        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        List<Subtask> subtasks = taskManager.getSubtaskList();
        assertEquals(1, subtasks.size(), "Wrong epic number.");
        int id = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();
        assertEquals(id, subtasks.getFirst().getId(), "Incorrect epic id");
        assertEquals("Test subtask #1", subtasks.getFirst().getName(), "Incorrect epic name");
    }

    @Test
    public void testGetSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME, TEST_DURATION, epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(gson.fromJson(response.body(), Subtask.class), taskManager.getSubtask(subtaskId), "Incorrect epic");
    }

    @Test
    public void testGetEpicList() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc", TEST_START_TIME, TEST_DURATION, epicId));
        taskManager.addSubtask(new Subtask("Test subtask #2", "desc", TEST_START_TIME.plus(TEST_DURATION),
                TEST_DURATION, epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(gson.fromJson(response.body(), new HttpTaskServerSubtasksTest.SubtaskListTypeToken().getType()),
                taskManager.getSubtaskList(), "Incorrect subtasks");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME, TEST_DURATION, epicId));
        Subtask updatesSubtask = new Subtask(subtaskId, "Updated", "Updated", TaskStatus.DONE,
                TEST_START_TIME, TEST_DURATION, epicId);
        String subtaskJson = gson.toJson(updatesSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        List<Subtask> subtasks = taskManager.getSubtaskList();
        assertEquals(1, subtasks.size(), "Wrong epic number.");
        assertEquals(updatesSubtask, subtasks.getFirst(), "Incorrect subtask");
    }

    @Test
    public void testRemoveSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME, TEST_DURATION, epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(0, taskManager.getSubtaskList().size(), "Subtask is still in the manager");
    }

    @Test
    public void getSubtaskShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME, TEST_DURATION, epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/777"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void updateEpicShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME, TEST_DURATION, epicId));
        Subtask updatedSubtask = new Subtask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        String epicJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/777"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void updateEpicShouldReturnBadRequestWhenPathIdDoesnotEqualBodyId() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        int subtaskId = taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME, TEST_DURATION, epicId));
        Subtask updatedSubtask = new Subtask("Updated", "Updated", TEST_START_TIME, TEST_DURATION, epicId);
        updatedSubtask.setId(subtaskId + 1);
        String subtaskJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + epicId))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void removeEpicShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc",
                TEST_START_TIME, TEST_DURATION, epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/777"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void addSubtaskShouldReturnNotAcceptableForOverlapedTasks() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc", TEST_START_TIME, TEST_DURATION, epicId));
        Subtask subtask = new Subtask("Test subtask #2", "desc", TEST_START_TIME, TEST_DURATION, epicId);
        String taskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Wrong code has been returned.");
        List<Subtask> subtasks = taskManager.getSubtaskList();
        assertEquals(1, subtasks.size(), "Wrong subtask number.");
    }

    @Test
    public void updateTaskShouldReturnNotAcceptableForOverlapedTasks() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc", TEST_START_TIME, TEST_DURATION, epicId));
        Subtask subtask = new Subtask("Test subtask #2", "desc",
                TEST_START_TIME.plus(TEST_DURATION), TEST_DURATION, epicId);
        int subtaskId = taskManager.addSubtask(subtask);
        Subtask updatedSubtask = new Subtask(subtaskId, "Updated", "Updated", TaskStatus.DONE,
                TEST_START_TIME, TEST_DURATION, epicId);
        String taskJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Wrong code has been returned.");
        assertEquals(subtask, taskManager.getSubtask(subtaskId), "Incorrect task");
    }
}