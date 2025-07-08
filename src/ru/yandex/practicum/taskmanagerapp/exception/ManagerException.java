package ru.yandex.practicum.taskmanagerapp.exception;

public class ManagerException extends RuntimeException {
    public ManagerException() {
        super();
    }

    public ManagerException(String msg) {
        super(msg);
    }
}
