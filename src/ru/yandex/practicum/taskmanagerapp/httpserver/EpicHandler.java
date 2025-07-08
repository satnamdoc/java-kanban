package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.taskmanagerapp.exception.BadJsonException;
import ru.yandex.practicum.taskmanagerapp.exception.InconsistentDataException;
import ru.yandex.practicum.taskmanagerapp.exception.NotFoundException;
import ru.yandex.practicum.taskmanagerapp.exception.TimeConflictException;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class EpicHandler extends ItemHandler {
    private TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
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
                    sendText(exchange, gson.toJson(taskManager.getEpic(optItemId.get())));
                }
                case GET_ALL_ITEMS -> {
                    sendText(exchange, gson.toJson(taskManager.getEpicList()));
                }
                case ADD_ITEM -> {
                    Epic epic = deserializeItem(exchange, Epic.class);
                    sendText(exchange, ID_TEMPLATE.formatted(taskManager.addEpic(epic)));
                }
                case UPDATE_ITEM -> {
                    Epic epic = deserializeItem(exchange, Epic.class);
                    if (epic.getId() != 0 && epic.getId() != optItemId.get()) {
                        sendBadRequest(exchange);
                    } else {
                        epic.setId(optItemId.get());
                        taskManager.updateEpic(epic);
                        sendOK(exchange);
                    }
                }
                case DELETE_ITEM -> {
                    taskManager.removeEpic(optItemId.get());
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