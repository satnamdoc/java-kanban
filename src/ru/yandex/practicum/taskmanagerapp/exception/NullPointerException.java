package ru.yandex.practicum.taskmanagerapp.exception;

public class NullPointerException extends RuntimeException {
  public NullPointerException() {
    super();
  }

  public NullPointerException(String message) {
    super(message);
  }
}
