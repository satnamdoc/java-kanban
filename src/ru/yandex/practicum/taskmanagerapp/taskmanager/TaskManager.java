package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
import ru.yandex.practicum.taskmanagerapp.task.Task;

import java.util.List;

public interface TaskManager {
    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubTask(SubTask subTask);

    boolean updateTask(Task task);

    boolean updateEpic(Epic epic);

    boolean updateSubTask(SubTask subTask);

    // при получении списков объектов из TaskManager хорошо бы делать клонирование...
    List<Task> getTaskList();

    List<Epic> getEpicList();

    List<SubTask> getSubTaskList();

    List<SubTask> getSubTasksOfEpic(int epicId);

    void clear();

    void clearTasks();

    void clearEpics();

    void clearSubTasks();

    Task getTask(int id);

    Task getEpic(int id);

    Task getSubTask(int id);

    boolean removeTask(int id);

    boolean removeEpic(int id);

    boolean removeSubTask(int id);

    List<Task> getHistory();
}
