package ru.yandex.practicum.taskmanagerapp.exception;

public class TimeConflictException extends ManagerException {
    public TimeConflictException() {
        super();
    }
    public TimeConflictException(String message) {
        super(message);
    }
}
