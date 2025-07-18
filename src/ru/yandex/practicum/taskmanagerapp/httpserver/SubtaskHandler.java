package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.taskmanagerapp.exception.BadJsonException;
import ru.yandex.practicum.taskmanagerapp.exception.InconsistentDataException;
import ru.yandex.practicum.taskmanagerapp.exception.NotFoundException;
import ru.yandex.practicum.taskmanagerapp.exception.TimeConflictException;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class SubtaskHandler extends ItemHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ItemHandler.Endpoint endpoint = getEndpoint(exchange);
        Optional<Integer> optItemId = getItemId(exchange);
        Gson gson = HttpTaskServer.getGson();

        try {
            switch (endpoint) {
                case GET_ITEM -> {
                    sendText(exchange, gson.toJson(taskManager.getSubtask(optItemId.get())));
                }
                case GET_ALL_ITEMS -> {
                    sendText(exchange, gson.toJson(taskManager.getSubtaskList()));
                }
                case ADD_ITEM -> {
                    Subtask subtask = deserializeItem(exchange, Subtask.class);
                    sendText(exchange, ID_TEMPLATE.formatted(taskManager.addSubtask(subtask)));
                }
                case UPDATE_ITEM -> {
                    Subtask subtask = deserializeItem(exchange, Subtask.class);
                    if (subtask.getId() != 0 && subtask.getId() != optItemId.get()) {
                        sendBadRequest(exchange);
                    } else {
                        subtask.setId(optItemId.get());
                        taskManager.updateSubtask(subtask);
                        sendOK(exchange);
                    }
                }
                case DELETE_ITEM -> {
                    taskManager.removeSubtask(optItemId.get());
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