package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
import ru.yandex.practicum.taskmanagerapp.task.Task;

import java.util.List;

public interface TaskManager {
    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubtask(Subtask subtask);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    List<Task> getTaskList();

    List<Epic> getEpicList();

    List<Subtask> getSubtaskList();

    List<Subtask> getEpicSubtasks(int epicId);

    void clear();

    void clearTasks();

    void clearEpics();

    void clearSubtasks();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    Task removeTask(int id);

    Epic removeEpic(int id);

    Subtask removeSubtask(int id);

    List<Task> getHistory();

    List<? super Task> getPrioritizedTasks();
}
