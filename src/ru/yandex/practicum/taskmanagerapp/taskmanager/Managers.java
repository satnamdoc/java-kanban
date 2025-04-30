package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.history.HistoryManager;
import ru.yandex.practicum.taskmanagerapp.history.InMemoryHistoryManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
