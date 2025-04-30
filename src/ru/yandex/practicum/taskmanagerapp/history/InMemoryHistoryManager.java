package ru.yandex.practicum.taskmanagerapp.history;

import ru.yandex.practicum.taskmanagerapp.task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> taskHistory = new ArrayList<>(HISTORY_SIZE);
    public static final int HISTORY_SIZE = 10;

    @Override
    public void add(Task task){
        if (taskHistory.size() == HISTORY_SIZE) {
            taskHistory.removeFirst();
        }
        taskHistory.add(task);
    }

    @Override
    public List<Task> getHistory(){
        return new ArrayList<>(taskHistory);
    }
}

