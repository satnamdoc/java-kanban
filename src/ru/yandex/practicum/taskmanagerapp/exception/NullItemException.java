package ru.yandex.practicum.taskmanagerapp.exception;

public class NullItemException extends ManagerException {
  public NullItemException() {
    super();
  }

  public NullItemException(String message) {
    super(message);
  }
}
