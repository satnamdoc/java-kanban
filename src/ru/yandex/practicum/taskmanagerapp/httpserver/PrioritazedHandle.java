package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class PrioritazedHandle extends BaseHttpHandler {
    TaskManager taskManager;

    public PrioritazedHandle(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        if (pathParts.length != 2 || !method.equals("GET")) {
            sendBadRequest(exchange);
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
        sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
    }
}