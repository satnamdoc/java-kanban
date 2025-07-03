package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
import ru.yandex.practicum.taskmanagerapp.task.Task;

import java.util.List;
import java.util.Optional;

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

    Optional<Task> getTask(int id);

    Optional<Epic> getEpic(int id);

    Optional<Subtask> getSubtask(int id);

    boolean removeTask(int id);

    boolean removeEpic(int id);

    boolean removeSubtask(int id);

    List<Task> getHistory();

    List<? super Task> getPrioritizedTasks();
}
