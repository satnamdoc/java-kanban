package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Task;
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

public class HttpTaskServerTasksTest {
    TaskManager taskManager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(taskManager);
    HttpClient client = HttpClient.newHttpClient();

    Gson gson = HttpTaskServer.getGson();

    class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    protected static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    protected static final Duration TEST_DURATION = Duration.ofDays(1);

    public HttpTaskServerTasksTest() throws IOException {
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
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test task #1", "Test task #1", LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        List<Task> tasks = taskManager.getTaskList();
        assertEquals(1, tasks.size(), "Wrong task number.");
        int id = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();
        assertEquals(id, tasks.getFirst().getId(), "Incorrect task id");
        assertEquals("Test task #1", tasks.getFirst().getName(), "Incorrect task name");
    }

    @Test
    public void testGetTask() throws IOException, InterruptedException {
        Task task = new Task("Test task #1", "Test task #1", LocalDateTime.now(), Duration.ofMinutes(5));
        int taskId = taskManager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(gson.fromJson(response.body(), Task.class), taskManager.getTask(taskId), "Incorrect task");
    }

    @Test
    public void testGetTaskList() throws IOException, InterruptedException {
        taskManager.addTask(new Task("Test task #1", "Test task #1", TEST_START_TIME, TEST_DURATION));
        taskManager.addTask(new Task("Test task #2", "Test task #2", TEST_START_TIME.plus(TEST_DURATION),
                TEST_DURATION));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(gson.fromJson(response.body(), new TaskListTypeToken().getType()),
                taskManager.getTaskList(), "Incorrect task");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        int taskId = taskManager.addTask(new Task("Test task #1", "Test task #1", TEST_START_TIME, TEST_DURATION));
        Task updatedTask = new Task(taskId, "Updated", "Updated", TaskStatus.DONE, TEST_START_TIME, TEST_DURATION);
        String taskJson = gson.toJson(updatedTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        List<Task> tasks = taskManager.getTaskList();
        assertEquals(1, tasks.size(), "Wrong task number.");
        assertEquals(updatedTask, tasks.getFirst(), "Incorrect task");
    }

    @Test
    public void testRemoveTask() throws IOException, InterruptedException {
        Task task = new Task("Test task #1", "Test task #1", LocalDateTime.now(), Duration.ofMinutes(5));
        int taskId = taskManager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Wrong code has been returned.");
        assertEquals(0, taskManager.getTaskList().size(), "Task is still in the manager");
    }

    @Test
    public void getTaskShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        taskManager.addTask(new Task("Test task #1", "Test task #1", LocalDateTime.now(), Duration.ofMinutes(5)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/777"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void updateTaskShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        taskManager.addTask(new Task("Test task #1", "Test task #1", TEST_START_TIME, TEST_DURATION));
        Task updatedTask = new Task("Updated", "Updated", TEST_START_TIME, TEST_DURATION);
        String taskJson = gson.toJson(updatedTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/777"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void updateTaskShouldReturnBadRequestWhenPathIdDoesnotEqualBodyId() throws IOException, InterruptedException {
        int taskId = taskManager.addTask(new Task("Test task #1", "Test task #1", TEST_START_TIME, TEST_DURATION));
        Task updatedTask = new Task(taskId + 1, "Updated", "Updated", TaskStatus.DONE, TEST_START_TIME, TEST_DURATION);
        String taskJson = gson.toJson(updatedTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Wrong code has been returned.");
    }

    @Test
    public void removeTaskShouldReturnNotFoundForNonexistingId() throws IOException, InterruptedException {
        taskManager.addTask(new Task("Test task #1", "Test task #1", LocalDateTime.now(), Duration.ofMinutes(5)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/777"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Wrong code has been returned.");
    }


    @Test
    public void addTaskShouldReturnNotAcceptableForOverlapedTasks() throws IOException, InterruptedException {
        taskManager.addTask(new Task("Test task #1", "Test task #1", TEST_START_TIME, TEST_DURATION));
        Task task = new Task("Test task #2", "Test task #2", TEST_START_TIME, TEST_DURATION);
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Wrong code has been returned.");
        List<Task> tasks = taskManager.getTaskList();
        assertEquals(1, tasks.size(), "Wrong task number.");
    }

    @Test
    public void updateTaskShouldReturnNotAcceptableForOverlapedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Test task #1", "Test task #1", TEST_START_TIME, TEST_DURATION);
        taskManager.addTask(task1);
        Task task2 = new Task("Test task #2", "Test task #2", TEST_START_TIME.plus(TEST_DURATION), TEST_DURATION);
        int taskId = taskManager.addTask(task2);
        Task updatedTask = new Task(taskId, "Updated", "Updated", TaskStatus.DONE, TEST_START_TIME, TEST_DURATION);
        String taskJson = gson.toJson(updatedTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Wrong code has been returned.");
        assertEquals(task2, taskManager.getTask(taskId), "Incorrect task");
    }
}