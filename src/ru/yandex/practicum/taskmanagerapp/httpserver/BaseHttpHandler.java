package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

abstract class BaseHttpHandler implements HttpHandler {
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        h.sendResponseHeaders(404, 0);
        h.close();
    }

    protected void sendHasOverlaps(HttpExchange h) throws IOException {
        h.sendResponseHeaders(406, 0);
        h.close();
    }

    protected void sendInternalError(HttpExchange h) throws IOException {
        h.sendResponseHeaders(500, 0);
        h.close();
    }

    protected void sendBadRequest(HttpExchange h) throws IOException {
        h.sendResponseHeaders(400, 0);
        h.close();
    }

    protected void sendOK(HttpExchange h) throws IOException {
        h.sendResponseHeaders(200, 0);
        h.close();
    }


}

