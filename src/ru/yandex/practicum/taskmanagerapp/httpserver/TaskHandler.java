package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.taskmanagerapp.exception.BadJsonException;
import ru.yandex.practicum.taskmanagerapp.exception.InconsistentDataException;
import ru.yandex.practicum.taskmanagerapp.exception.NotFoundException;
import ru.yandex.practicum.taskmanagerapp.exception.TimeConflictException;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class TaskHandler extends ItemHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange);
        Optional<Integer> optItemId = getItemId(exchange);
        Gson gson = HttpTaskServer.getGson();

        try {
            switch (endpoint) {
                case GET_ITEM -> {
                    sendText(exchange, gson.toJson(taskManager.getTask(optItemId.get())));
                }
                case GET_ALL_ITEMS -> {
                    sendText(exchange, gson.toJson(taskManager.getTaskList()));
                }
                case ADD_ITEM -> {
                    Task task = deserializeItem(exchange, Task.class);
                    sendText(exchange, ID_TEMPLATE.formatted(taskManager.addTask(task)));
                }
                case UPDATE_ITEM -> {
                    Task task = deserializeItem(exchange, Task.class);
                    if (task.getId() != 0 && task.getId() != optItemId.get()) {
                        sendBadRequest(exchange);
                    } else {
                        task.setId(optItemId.get());
                        taskManager.updateTask(task);
                        sendOK(exchange);
                    }
                }
                case DELETE_ITEM -> {
                    taskManager.removeTask(optItemId.get());
                    sendOK(exchange);
                }
                case UNKNOWN -> {
                    sendBadRequest(exchange);
                }
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (TimeConflictException | InconsistentDataException e) {
            sendHasOverlaps(exchange);
        } catch (BadJsonException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}
