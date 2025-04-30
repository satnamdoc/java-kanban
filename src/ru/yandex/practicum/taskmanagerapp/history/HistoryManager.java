package ru.yandex.practicum.taskmanagerapp.history;

import ru.yandex.practicum.taskmanagerapp.task.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    List<Task> getHistory();
}
