package ru.yandex.practicum.taskmanagerapp.exception;

public class InconsistentDataException extends RuntimeException {
    public InconsistentDataException() {
        super();
    }

    public InconsistentDataException(String message) {
        super(message);
    }
}
