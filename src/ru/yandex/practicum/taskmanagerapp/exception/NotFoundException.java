package ru.yandex.practicum.taskmanagerapp.exception;

public class NotFoundException extends ManagerException {
    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }
}
