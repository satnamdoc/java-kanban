package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
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

public class HttpTaskServerEpicsTest {
    TaskManager taskManager = new InMemoryTaskManager();
    HttpTaskServer epicServer = new HttpTaskServer(taskManager);
    HttpClient client = HttpClient.newHttpClient();

    Gson gson = HttpTaskServer.getGson();

    class EpicListTypeToken extends TypeToken<List<Epic>> {
    }

    protected static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    protected static final Duration TEST_DURATION = Duration.ofDays(1);

    public HttpTaskServerEpicsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        List<Epic> epics = taskManager.getEpicList();
        assertEquals(1, epics.size(), "Wrong epic number.");
        int id = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();
        assertEquals(id, epics.getFirst().getId(), "Incorrect epic id");
        assertEquals("Test epic #1", epics.getFirst().getName(), "Incorrect epic name");
    }

    @Test
    public void testGetEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc", TEST_START_TIME, TEST_DURATION, epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(gson.fromJson(response.body(), Epic.class), taskManager.getEpic(epicId), "Incorrect epic");
    }

    @Test
    public void testGetEpicList() throws IOException, InterruptedException {
        int epicId1 = taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc", TEST_START_TIME, TEST_DURATION, epicId1));
        int epicId2 = taskManager.addEpic(new Epic("Test epic #2", "Test epic #2"));
        taskManager.addSubtask(new Subtask("Test subtask #2", "desc", TEST_START_TIME.plus(TEST_DURATION),
                TEST_DURATION, epicId2));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(gson.fromJson(response.body(), new EpicListTypeToken().getType()),
                taskManager.getEpicList(), "Incorrect epic");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc", TEST_START_TIME, TEST_DURATION, epicId));
        epic = taskManager.getEpic(epicId);
        Epic updatedEpic = new Epic(epicId, "Updated", "Updated", epic.getStatus(), epic.getStartTime().get(),
                epic.getDuration(), epic.getEndTime().get(), epic.getSubtaskIds());
        String epicJson = gson.toJson(updatedEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        List<Epic> epics = taskManager.getEpicList();
        assertEquals(1, epics.size(), "Wrong epic number.");
        assertEquals(updatedEpic, epics.getFirst(), "Incorrect epic");
    }

    @Test
    public void testRemoveEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic #1", "Test epic #1");
        int epicId = taskManager.addEpic(epic);
        taskManager.addSubtask(new Subtask("Test subtask #1", "desc", TEST_START_TIME, TEST_DURATION, epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(0, taskManager.getEpicList().size(), "Epic is still in the manager");
    }

    @Test
    public void getEpicShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/777"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void updateEpicShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));
        Epic updatedEpic = new Epic("Updated", "Updated");
        String epicJson = gson.toJson(updatedEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/777"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void updateEpicShouldReturnBadRequestWhenPathIdDoesnotEqualBodyId() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));
        Epic updatedEpic = new Epic("Updated", "Updated");
        updatedEpic.setId(epicId + 1);
        String epicJson = gson.toJson(updatedEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void removeEpicShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        taskManager.addEpic(new Epic("Test epic #1", "Test epic #1"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/777"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }
}