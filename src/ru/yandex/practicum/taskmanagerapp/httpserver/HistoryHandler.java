package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        if (pathParts.length != 2 || !method.equals("GET")) {
            sendBadRequest(exchange);
        }

        Gson gson = HttpTaskServer.getGson();
        sendText(exchange, gson.toJson(taskManager.getHistory()));
    }
}
