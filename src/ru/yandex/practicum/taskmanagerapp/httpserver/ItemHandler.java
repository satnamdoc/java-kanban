package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.sun.net.httpserver.HttpExchange;

import java.util.Optional;

abstract class ItemHandler extends BaseHttpHandler {
    protected enum Endpoint {
        GET_ITEM,
        GET_ALL_ITEMS,
        ADD_ITEM,
        UPDATE_ITEM,
        DELETE_ITEM,
        UNKNOWN
    }

    protected static Endpoint getEndpoint(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        if (pathParts.length == 2) {
            return switch (method) {
                case "GET" -> Endpoint.GET_ALL_ITEMS;
                case "POST" -> Endpoint.ADD_ITEM;
                default -> Endpoint.UNKNOWN;
            };
        } else if (pathParts.length == 3) {
            if (getItemId(exchange).isEmpty()) {
                return Endpoint.UNKNOWN;
            }
            return switch (method) {
                case "GET" -> Endpoint.GET_ITEM;
                case "POST" -> Endpoint.UPDATE_ITEM;
                case "DELETE" -> Endpoint.DELETE_ITEM;
                default -> Endpoint.UNKNOWN;
            };
        } else {
            return Endpoint.UNKNOWN;
        }
    }

    protected static Optional<Integer> getItemId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}