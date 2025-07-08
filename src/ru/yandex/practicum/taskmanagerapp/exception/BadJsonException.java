package ru.yandex.practicum.taskmanagerapp.exception;

public class BadJsonException extends ManagerException {
    public BadJsonException() {
        super();
    }

    public BadJsonException(String message) {
        super(message);
    }
}
